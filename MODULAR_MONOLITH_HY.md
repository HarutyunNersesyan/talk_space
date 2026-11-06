# TalkSpace — մոդուլային մոնոլիտ

Backend-ը մեկ Spring Boot հավելված է՝ Maven parent նախագծով և 10 առանձին Maven մոդուլներով, մեկ executable JAR-ով և ընդհանուր PostgreSQL բազայով։ Կոդը բաժանված է գործառնական մոդուլների։ Frontend-ը մնում է առանձին React հավելված։ Redis-ը, Elasticsearch-ը և մյուս արտաքին ենթակառուցվածքները պահպանում են իրենց նախկին կարգավորումները։

## Կոդի կառուցվածքը

Յուրաքանչյուր մոդուլ ունի իր `pom.xml`-ը և `src/main/java`-ն։ Արմատային `pom.xml`-ը `packaging=pom` aggregator/parent է. արմատում `src` չկա։

8 գործառնական մոդուլներին ավելանում են `talkspace-shared` ենթակառուցվածքային և `talkspace-app` գործարկվող մոդուլները։

| Պանակ | Պատասխանատվություն |
| --- | --- |
| `talkspace-users/` | Հաշիվներ, պրոֆիլներ, նկարներ, հոբբիներ, մասնագիտություններ, տեղանք, մոդերացիա |
| `talkspace-auth/` | Մուտք, JWT, refresh session-ներ, token rotation, logout, socket authentication |
| `talkspace-network/` | Connection request-ներ, ընդունում, բլոկավորում, հասանելիության ստուգումներ |
| `talkspace-discovery/` | Matching, դիտված/բաց թողնված առաջարկներ, որոնում, Elasticsearch indexing |
| `talkspace-chat/` | Նամակներ, խմբագրում, ջնջում, կարդալու կարգավիճակ, չատի REST/STOMP endpoints |
| `talkspace-notifications/` | Ծանուցումներ, unread count և real-time առաքում |
| `talkspace-feedback/` | Կարծիքներ և գնահատականներ |
| `talkspace-presence/` | Online կարգավիճակ և socket կապի lifecycle |
| `talkspace-app/` | Spring/security/WebSocket կազմաձևում, seed, ընդհանուր request filters և մի քանի մոդուլ միավորող հին controller-ներ |
| `talkspace-shared/` | Ընդհանուր սխալներ, logging/monitoring, Redis և rate-limit ենթակառուցվածք |

Յուրաքանչյուր մոդուլ ունի՝

- `api` — մյուս մոդուլներին հասանելի ծառայություններ, DTO-ներ, իրադարձություններ և անհրաժեշտ համատեղ մոդելներ։
- `internal` — տվյալ մոդուլի repository-ները, իրականացումը, controller-ները և ներքին օժանդակ դասերը։

Մեկ մոդուլից մյուսի `internal` դասերին դիմելն արգելված է ճարտարապետական թեստերով։ `bootstrap`-ը հավելվածը հավաքող շերտն է և կարող է միացնել ներքին security բաղադրիչները։

## Թույլատրված կախվածությունները

Բոլոր մոդուլները կարող են օգտվել `shared`-ից։ Գործառնական կախվածությունները՝

| Մոդուլ | Թույլատրված մյուս մոդուլներ |
| --- | --- |
| users | — |
| auth | users |
| notifications | users |
| network | users, notifications |
| discovery | users, network |
| chat | users, network |
| feedback | users |
| presence | users, network |

`shared`-ը չի կախված մոդուլներից կամ `bootstrap`-ից։ Մոդուլները չեն կախված `bootstrap`-ից։ Շրջանաձև կախվածությունները նույնպես ստուգվում են ArchUnit-ով։

## Տվյալների սեփականություն և հաղորդակցություն

Այլ մոդուլները օգտատիրոջ տվյալները կարդում են `users.api.UserDirectory`-ով։ Network-ը վերադարձնում է ընդունված կապերի անունները, իսկ users մոդուլը իր repository-ով բեռնում է համապատասխան ակտիվ պրոֆիլները։ Չատի մոդուլը չի ներմուծում users կամ network repository-ները։

Հաշիվ ջնջելիս users մոդուլը նույն transaction-ի մեջ հրապարակում է `UserDeleting` իրադարձությունը։ Chat, auth, network, notifications, discovery և feedback մոդուլներից յուրաքանչյուրը մաքրում է իր տվյալները, ապա users-ը ջնջում է հաշիվը։ Listener-ները synchronous են և պահանջում են արդեն բաց transaction (`MANDATORY`)։ Դրանք չպետք է դարձնել `@Async` կամ after-commit listener. տվյալների բազայի մաքրումն ու հաշվի ջնջումը պետք է միասին հետ գլորվեն սխալի դեպքում։ Redis cache-ի հեռացումը հետ չի գլորվում, բայց այն վերականգնվող cache է։

Ապաբլոկավորման ժամանակ network-ը հրապարակում է `PairUnblocked`, իսկ discovery-ն վերակայում է այդ զույգի matching պատմությունը։

## Սահմանների գործնական սահմանափակումը

Սա առանձին Maven մոդուլներով մոդուլային մոնոլիտ է։ Մոդուլները կոմպիլացվում և հավաքվում են առանձին library JAR-երի մեջ, իսկ `talkspace-app`-ը դրանք միավորում է մեկ գործարկվող JAR-ի մեջ։ Maven կախվածությունները սահմանված են յուրաքանչյուր մոդուլի `pom.xml`-ում։ Մոդուլների `api` և `internal` package-ների սահմանները լրացուցիչ ստուգվում են ArchUnit-ով։ Համատեղելիության համար users-ի JPA aggregate-ները դեռ հասանելի են `users.api.model`-ում, և որոշ public ծառայություններ վերադարձնում են entity-ներ։ Այլ մոդուլները պետք է այդ օբյեկտները համարեն միայն կարդալու համար. սա Java տիպերով դեռ անփոփոխելի չի դարձված։ Discovery-ի արդյունավետ read-query-ները նաև կարդում են users/network աղյուսակները։ ArchUnit-ը ստուգում է Java կախվածությունները, ոչ SQL տողերի աղյուսակային սահմանները։

Ամբողջական persistence isolation-ի հաջորդ փուլը կարող է լինել immutable DTO-ներ և առանձին read-model-ներ։ Ներկա փոփոխությունը չի ներմուծում առանձին database-ներ կամ ծառայությունների միջև HTTP կանչեր։

## Համատեղելիություն

- REST և STOMP ուղիները պահպանված են։ Frontend-ի API հասցեները փոխելու կարիք չկա։
- JWT access/refresh token-ի ընթացքը և cookie-ի կարգավորումները պահպանված են։
- Չատի edit/delete/read գործողությունները պահպանված են։
- Աղյուսակների անունները և Flyway V1–V4 migration-ները պահպանված են։ Այս վերադասավորումը նոր migration չի պահանջում։
- Հեռացվել է User-ից դեպի ChatMessage հակադարձ JPA collection-ը. չատի տվյալների ջնջումն այժմ պատկանում է chat մոդուլին։

## Փոխարինում և գործարկում

1. Պահիր քո տեղային գաղտնի կարգավորումները և վերբեռնված նկարները։
2. Արխիվը բացիր նոր պանակում և տեղափոխիր անհրաժեշտ տեղային կարգավորումները։ Հին `src`-ի վրա պարզապես մի ավելացրու նոր ֆայլերը, քանի որ հին փաթեթների դասերը կարող են կրկնվել։
3. Backend-ի պանակում աշխատեցրու `./mvnw clean test` (Windows՝ `mvnw.cmd clean test`)։
4. Արմատային պանակում հավաքիր `./mvnw clean package`, ապա գործարկիր `java -jar talkspace-app/target/talkspace-app.jar`։ Windows-ում Maven հրամանը՝ `mvnw.cmd clean package`։
5. Օգտագործիր գործող frontend-ը՝ նախկին ձևով։

## Ստուգումներ

Վերջնական հավաքումը հաջողվել է։ Բոլոր մոդուլների թեստերի արդյունքները՝ [TEST_RESULTS_HY.md](TEST_RESULTS_HY.md)։

`MavenReactorTest`-ը ստուգում է առանձին Maven source tree-ները, executable packaging-ի միակ սեփականատիրոջը և բոլոր feature JAR-երի ներառումը architecture scan-ում։ `ModuleBoundariesTest`-ը ստուգում է մոդուլների կախվածությունները, `internal` սահմանները, շրջափուլերի բացակայությունը և արտադրական դասերի պատկանելությունը։ Գործող HTTP թեստերը ստուգում են նաև չատի, connection-ների, բլոկավորման և refresh session-ների համատեղելիությունը։ Հաշվի ջնջման թեստերը ստուգում են մոդուլների մաքրումը, մյուս օգտատիրոջ տվյալների պահպանումը և սխալի դեպքում database rollback-ը։

PostgreSQL/Elasticsearch Docker ինտեգրացիոն թեստերը առանձին են՝ `./mvnw -Pintegration verify`։ Այս փաթեթի ստուգումները H2/Spring միջավայրում են. իրական երկու browser-ով փորձարկում չի կատարվել։

## Maven և IDE

- IntelliJ IDEA-ում բացիր backend-ի արմատային `pom.xml`-ը որպես Maven նախագիծ և կատարիր Reload All Maven Projects։ Մոդուլները կերևան առանձին։
- Սկսելու հիմնական դասը՝ `talkspace-app/src/main/java/com/talk_space/TalkSpaceApplication.java`։ IDE run configuration-ի module/classpath-ը՝ `talkspace-app`։
- `application.properties`, logging և Flyway migration-ները գտնվում են `talkspace-app/src/main/resources/`-ում։
- Միայն chat-ը և նրա կախվածությունները ստուգելու համար՝ `./mvnw -pl talkspace-chat -am test`։
- Maven-ով development գործարկման համար նախ արմատում արա `./mvnw install -DskipTests`, ապա `./mvnw -pl talkspace-app spring-boot:run`։ `spring-boot:run`-ը մի գործարկիր parent-ի վրա `-am`-ով, քանի որ library մոդուլները գործարկվող հավելվածներ չեն։
- `images/` պանակը մնում է արմատում։ Պահպանիր քո `IMAGE_DIRECTORY` կարգավորումը։
- Dockerfile-ը հավաքում է ամբողջ reactor-ը և container է տեղափոխում միայն `talkspace-app/target/talkspace-app.jar`-ը։
