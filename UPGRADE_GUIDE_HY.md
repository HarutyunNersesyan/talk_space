> Maven մոդուլների, ֆայլերի տեղադրության և գործարկման ընթացիկ հրահանգները՝ [README.md](README.md)։

> Նոր matching v2-ը փոխարինում է ստորև նկարագրված նախկին հաշվարկն ու հայտերի վերացուցադրման կանոնները։ Տե՛ս [MATCHING_V2_HY.md](MATCHING_V2_HY.md)։

# TalkSpace backend — բարելավումների ուղեցույց

Իրականացման հերթականությունը՝ Smart Matching → Connections & Notifications → Redis → Search & Observability → Testing & CI։

Այս տարբերակը օգտագործում է միայն Connect → Accept → Chat հոսքը։ Like-ի նախկին endpoint-ներն ու կոդը հեռացված են։ Հայտերի պատասխանները ներառում են հանրային պրոֆիլը, իսկ չաթը պահանջում է ACCEPTED կապ։ Մանրամասները՝ CONNECT_ONLY_HY.md-ում։ Ֆրոնթենդը ներառված է առանձին talk_space_ui.zip-ում։

## 1. Smart Matching

Գործող URL-ներ՝

- `GET /api/public/user/searchByHobbies/{userName}`
- `GET /api/public/user/searchBySpecialities/{userName}`

Ալգորիթմը օգտագործում է weighted Jaccard similarity։ Ընտրված հատկանիշի կշիռը `1` է, նրա անմիջական ծնող կատեգորիայինը՝ `0.35`։ Երկու պրոֆիլների ընդլայնված հատկանիշների համար հաշվվում է `sum(min(weights)) / sum(max(weights))`։ Նույն հետաքրքրությունը բարձր գնահատական է ստանում, ընդհանուր ծնող ունեցող տարբեր հետաքրքրությունները՝ մասնակի։ Կրկնվող հատկանիշները միավորը չեն բարձրացնում։ Ավելի հեռու նախնիների կապը այս տարբերակում չի հաշվարկվում։

Հոբբիներով որոնելիս հոբբիների գնահատականը կազմում է 80%, մասնագիտություններինը՝ 20%։ Մասնագիտություններով որոնելիս առաջնահերթությունը հակառակն է։ Եթե երկրորդ խմբի տվյալները բացակայում են, հիմնական գնահատականը կիրառվում է ամբողջությամբ։ Հիմնական խմբում կապ չունեցող պրոֆիլ չի առաջարկվում։

Ընտրվում են միայն ակտիվ, հաստատված email ունեցող USER հաշիվները։ Բացառվում են հենց օգտատերը, հաստատված կապերը և ցանկացած ուղղությամբ արգելափակված հաշիվները։ Նախ ընտրվում են վերջերս չցուցադրվածները։ Շարքն ավարտվելուց հետո սկսվում է նոր շրջան, որպեսզի չընդունված կամ մերժված հայտերը մշտապես չթաքցնեն պրոֆիլը։ 404 է վերադարձվում, երբ հասանելի համապատասխան թեկնածու չկա։ Պատմությունը պահվում է `match_impressions` աղյուսակում։ Միաժամանակյա հարցումները նույն օգտատիրոջ համար հերթագրվում են PostgreSQL row lock-ով։

Գնահատվում են առավելագույնը 500 նախապես ընտրված թեկնածուներ։ Մեծ բազայի դեպքում սա սահմանափակ թեկնածուների միջից լավագույն ընտրությունն է, ոչ ամբողջ բազայի գլոբալ օպտիմումը։ Սահմանը կարգավորվում է `app.matching.candidate-limit`-ով։ Արտաքին AI ծառայություն կամ ուսուցման տվյալներ պետք չեն։

Կոդ՝ `matching/`։ Կշիռները և պատմության ժամկետը՝ `application.properties`-ում։ Համապատասխանության տոկոսը պատասխանին չի ավելացվել՝ գործող frontend DTO-ն պահպանելու համար։

## 2. Connections & Notifications

Նոր REST հարցումները օգտագործում են նույն JWT-ն՝ `Authorization: Bearer <token>`։ Գործող օգտատերը որոշվում է token-ից։ Request body-ում ուղարկվում է միայն մյուս օգտատիրոջ անունը։

| Մեթոդ | URL | Նպատակ |
| --- | --- | --- |
| POST | `/api/public/connections` | Ուղարկել հայտ՝ `{"userName":"bob"}` |
| GET | `/api/public/connections?page=0&size=20` | Սեփական կապերի և հայտերի ցուցակ |
| POST | `/api/public/connections/{id}/accept` | Ստացողը ընդունում է հայտը |
| POST | `/api/public/connections/{id}/reject` | Ստացողը մերժում է հայտը |
| POST | `/api/public/connections/{id}/cancel` | Ուղարկողը չեղարկում է հայտը |
| POST | `/api/public/connections/{id}/remove` | Մասնակիցներից մեկը հեռացնում է ընդունված կապը |
| GET | `/api/public/connections/pending-count` | Մուտքային PENDING հայտերի քանակ |
| GET | `/api/public/notifications?page=0&size=20` | Սեփական notifications inbox |
| GET | `/api/public/notifications/unread-count` | Չկարդացված notifications-ի քանակ՝ `{"count":3}` |
| POST | `/api/public/notifications/{id}/read` | Նշել կարդացված |
| POST | `/api/public/notifications/read-all` | Սեփական բոլոր notifications-ը նշել կարդացված |

Էջերը սկսվում են 0-ից, չափը՝ 1–100։ Նույն ուղղությամբ PENDING հայտը կրկնելիս նոր տող/notification չի ստեղծվում։ Հակառակ ուղղությամբ արդեն եկած հայտը պետք է ընդունել։ Զույգի գործողությունները կողպվում են նույն հերթականությամբ՝ մրցակցող հարցումների դեպքում կրկնօրինակներից խուսափելու համար։

Ընդունված կապերը հայտնվում են գործող `/api/public/chat/conversations/{userName}` պատասխանում։ Միայն ACCEPTED կապն է թույլ տալիս հաղորդագրություն ուղարկել և պատմություն կարդալ։ Կապի հեռացումը պահպանում է պատմության տվյալները, իսկ մուտքը վերականգնվում է նոր հայտի ընդունումից հետո։

Notifications-ը պահվում են բազայում։ WebSocket առաքումը կատարվում է միայն հաջող commit-ից հետո։ Անջատված հաճախորդը վերամիանալիս կարդում է inbox-ը REST-ով։ Notification unread count-ը առանձին է գործող chat-message unread count-ից։

### Նոր WebSocket կապ

Ֆրոնթենդը օգտագործում է `/ws-secure` SockJS endpoint-ը։ `/ws` համատեղելի endpoint-ը նույնպես պահանջում է JWT և թույլատրում է միայն սահմանված private destinations-ը։ STOMP CONNECT header-ում փոխանցել `Authorization: Bearer <token>`։ Նոր կապի principal-ը token-ով ստուգված `userName`-ն է։

- Subscribe՝ `/user/queue/connection-notifications`։ Ստացվում է պահպանված notification-ի JSON-ը։ Դրանից հետո կարելի է նորից կարդալ unread count-ը։
- Publish՝ `/app/presence.heartbeat`, body՝ `{}`։ Ուղարկել յուրաքանչյուր 30 վայրկյանը մեկ։
- CONNECT-ից հետո կարդալ REST inbox-ը, որպեսզի բաց թողնված իրադարձությունները վերականգնվեն։

Ծանուցման օրինակ՝ `{"id":1,"recipient":"bob","actor":"alice","type":"CONNECTION_REQUEST","connectionId":5,"createdAt":"...","readAt":null}`։

Թույլատրված origin-ը շարունակում է լինել `http://localhost:3000`։ Ֆրոնթենդում միացված են կապերը, լիարժեք պրոֆիլների քարտերը, սև ցուցակը, private chat-ը և ծանուցումները։ Simple broker-ի live notifications-ը նախատեսված են մեկ backend instance-ի համար. մի քանի instance-ի ընդհանուր live առաքման համար հետագայում պետք է broker relay կամ pub/sub։

## 3. Redis

Տեղային գործարկման ժամանակ `REDIS_ENABLED=false` է։ Docker Compose-ում՝ `true`։

- Cache՝ matching անող օգտատիրոջ հատկանիշները՝ 30 վայրկյան TTL-ով։ Թեկնածուները, կապերը, սև ցուցակը և impressions-ը ամեն հարցման ժամանակ ստուգվում են բազայում։ Հոբբի/մասնագիտություն թարմացնելիս սեփական cache-ը մաքրվում է։
- Rate limiting՝ Redis Lua script-ով ատոմային fixed window։ Login/signup/forgot-password խմբին՝ 10 հարցում/րոպե, matching/connections/search խմբերից յուրաքանչյուրին՝ 60 հարցում/րոպե։ Սահմանը գերազանցելիս՝ HTTP 429 և `Retry-After`։
- Presence՝ առանձին session-ներ, 90 վայրկյան lease։ Մի tab փակելը մյուս tab-ը offline չի դարձնում։ Heartbeat-ի դադարից հետո online վիճակը մարում է։
- `GET /api/public/presence/{userName}` → `{"online":true}`։

Redis անջատված լինելու կամ խափանման դեպքում cache-ը շրջանցվում է, rate limiting-ը fail-open է, իսկ presence-ը հաշվարկվում է տվյալ instance-ի հիշողությունում։ Խափանումների հաշվիչները հասանելի են metrics-ում։ Redis-ից վերականգնվելուց հետո presence-ը թարմացվում է հաջորդ heartbeat-ով։

## 4. Search & Observability

`GET /api/public/search/users?q=music&page=0&size=20`։ Որոնվում են username-ը, անունը, ազգանունը, հոբբիները, մասնագիտությունները և aboutMe-ն։ Գործում է fuzzy multi-field query։ Որոնումը չի փոխարինում հին matching endpoint-ներին։

`SEARCH_ENABLED=false` դեպքում նոր որոնման endpoint-ը վերադարձնում է 503։ Elasticsearch-ը միացնելուց հետո ինդեքսավորումը սկսվում է մոտ 10 վայրկյանից և կրկնվում է յուրաքանչյուր 60 վայրկյանը մեկ։ Սինքրոնացումը փուլային ամբողջական վերաինդեքսավորում է՝ 500-ական տողերով, PostgreSQL advisory lock-ով մեկ writer ապահովելով։ Դինամիկ փոփոխությունները ինդեքսում հայտնվում են հաջորդ սինքրոնացման ժամանակ։

Գաղտնաբառ, email, pin, կոնկրետ հասցե կամ հաղորդագրությունների բովանդակություն որոնման ինդեքսում չի պահվում։ Արդյունքները կրկին ստուգվում են PostgreSQL-ում, որպեսզի արդեն արգելափակված կամ ջնջված հաշիվը հին ինդեքսից չվերադարձվի։ `indexedTotal`-ը ինդեքսի հաշվարկն է և կարճ ժամանակ կարող է տարբերվել վերադարձվող ակտիվ պրոֆիլների քանակից։

JSON logs՝ `logs/talk-space-json.log`։ HTTP request-ի correlation ID-ն վերադարձվում է `X-Correlation-ID` header-ով։ Նոր HTTP logging-ը չի գրանցում Authorization/Cookie header-ներ և query արժեքներ։ Filebeat-ը կարդում է JSON-ը և փոխանցում Elasticsearch-ին։

Kibana՝ `http://localhost:5601` → Stack Management → Saved Objects → Import → `observability/kibana-dashboards.ndjson`։ Ներառված են HTTP հարցումների քանակի, միջին տևողության և status code-երի dashboard-ի saved objects։ Դրանք պետք է ներմուծել գործարկված Kibana-ում։ Այս միջավայրում Kibana-ի live import-ը չի ստուգվել։

Metrics՝ `/actuator/metrics` և `/actuator/prometheus`։ Պահանջվում է ADMIN authority ունեցող JWT։ Health-ը հասանելի է առանց login-ի՝ `/actuator/health`։ Ավելացված են matching տևողություն/արդյունք, search sync/query, cache hit/miss, Redis failures և rate-limit rejection հաշվիչներ։

## 5. Գործարկում և ստուգումներ

### IntelliJ / Maven

JDK 17, Maven։ Բացել `pom.xml`-ը։ PostgreSQL բազան՝ `talk_space`, տեղային կարգավորումները՝ `application.properties`։

```bash
mvn test
mvn spring-boot:run
```

Windows-ում կարելի է օգտագործել `mvnw.cmd`, Linux/macOS-ում՝ `sh mvnw`։ Redis/Elasticsearch-ի գործարկման համար՝

```bash
docker compose up -d db redis elasticsearch kibana filebeat
```

Հետո backend-ի environment-ում տալ `REDIS_ENABLED=true`, `SEARCH_ENABLED=true`։ Հասցեների լռելյայն արժեքները localhost են։

Տվյալների պահպանման համար `ddl-auto=create-drop`-ը փոխարինված է `update`-ով։ Նոր աղյուսակներն ու index-ները ստեղծվում են startup-ին։ Demo seed-ը կատարվում է միայն դատարկ համապատասխան աղյուսակի դեպքում։ Կարելի է անջատել՝ `DEMO_SEED_ENABLED=false`։

### Docker

```bash
docker compose up --build -d
```

Գործարկվում են backend, PostgreSQL, Redis, Elasticsearch, Kibana և Filebeat։ Frontend-ը լրացուցիչ profile է՝ `docker compose --profile frontend up --build -d`, երբ հարևան `talk_space_ui` պանակը հասանելի լինի։

Տվյալների, logs-ի և images-ի համար օգտագործվում են volumes։ Սովորական `docker compose down`-ը տվյալների volumes-ը չի ջնջում։ Runtime image-ը աշխատում է non-root օգտատիրոջով։ Այս Compose-ը տեղային մշակման համար է։

Նկարների upload/read ուղին կարգավորվում է `app.images.directory`-ով։ Windows-ի հին լռելյայն ուղին պահպանված է, իսկ Docker-ում `IMAGE_DIRECTORY=/app/images` է։ Read endpoint-ի ներսում ուղու հաշվարկը փոխանցված է ImageService-ին՝ URL-ը և response-ը պահպանելով։

### Tests և CI

```bash
mvn test
mvn -Pintegration verify
mvn package
```

`mvn test`-ը Docker չի պահանջում․ ներառում է JUnit 5, Mockito, H2 Spring context և HTTP integration ստուգումներ։ `mvn -Pintegration verify`-ը Docker-ով գործարկում է PostgreSQL/Redis/Elasticsearch Testcontainers-ը՝ իրական SQL upsert-ի, մրցակցող matching-ի, connection flow-ի, Redis expiry-ի և Elasticsearch sync-ի համար։ Docker-ի բացակայությունը չի թաքցվում skipped կանաչ թեստերով. այս profile-ը այդ դեպքում չի անցնի։

`.github/workflows/backend-ci.yml`-ում unit/context tests → Testcontainers tests → Docker image build։ Image-ը չի հրապարակվում և deployment չի կատարվում։ Workflow-ն աշխատում է repository-ի արմատում։

Տեղային ստուգման արդյունքները՝ `VALIDATION.md`։ GitHub Actions-ը կգործարկվի այս ֆայլերը repository-ում տեղադրելուց հետո։

## Տեխնիկական աղբյուրներ

- Spring WebSocket token authentication: https://docs.spring.io/spring-framework/reference/web/websocket/stomp/authentication-token-based.html
- Spring Data Redis Lua scripting: https://docs.spring.io/spring-data/redis/reference/redis/scripting.html
- Elasticsearch multi-match query: https://www.elastic.co/docs/reference/query-languages/query-dsl/query-dsl-multi-match-query
- Elasticsearch refresh semantics: https://www.elastic.co/docs/reference/elasticsearch/rest-apis/refresh-parameter
