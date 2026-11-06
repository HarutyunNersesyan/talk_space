# Բազմամոդուլային backend-ի ստուգումներ — 2026-11-05

- Java 17 / Spring Boot 3.3.3։
- Maven reactor-ի `./mvnw verify` հրամանը հաջողությամբ ավարտվել է բոլոր 10 մոդուլների համար։
- **132 թեստ, 0 ձախողում, 0 սխալ, 0 բաց թողնված թեստ**։
- `talkspace-app/target/talkspace-app.jar`-ը հավաքված է որպես executable Spring Boot JAR և ներառում է 9 առանձին TalkSpace library JAR։ Արխիվում տրամադրվում է source նախագիծը. JAR-ը ստանալու համար գործարկիր `mvnw.cmd clean verify`։

| Մոդուլ | Անցած թեստեր |
| --- | ---: |
| talkspace-app | 59 |
| talkspace-auth | 6 |
| talkspace-chat | 3 |
| talkspace-discovery | 23 |
| talkspace-feedback | 3 |
| talkspace-network | 8 |
| talkspace-notifications | 3 |
| talkspace-presence | 2 |
| talkspace-shared | 0 |
| talkspace-users | 25 |

## Ինչ է ստուգվել

- 6 ArchUnit ստուգում՝ Java մոդուլների սահմաններ, կախվածությունների գրաֆ, շրջափուլերի բացակայություն և bootstrap/shared բաժանում։
- 3 Maven կառուցվածքի ստուգում՝ առանձին `pom.xml` և `src`, root aggregator-ի `src`-ի բացակայություն, միակ executable app և բոլոր feature JAR-երի տեսանելիություն architecture scan-ին։
- Spring/MockMvc/H2՝ REST համատեղելիություն, chat edit/delete/read, connections, blocking, authentication, refresh session rotation/logout։
- Հաշվի ջնջման մոդուլային cleanup և տվյալների հետգլորում սխալի դեպքում։
- Նախորդ փաթեթի 152 production Java source-ների մարմիններն անփոփոխ են, բացի չօգտագործվող import-ների հեռացումից։ 7 production resource ֆայլերը պահպանված են byte-for-byte, ներառյալ կարգավորումներն ու migration-ները։

## Հավաքված հավելվածի գործարկում

Հավաքված Boot archive-ը գործարկվել է մեկուսացված H2 բազայով՝ test configuration և արտաքին H2 driver օգտագործելով `PropertiesLauncher`-ի միջոցով։ Tomcat-ը հաջողությամբ մեկնարկել է, իսկ առանց authentication-ի inbox հարցումը վերադարձրել է սպասվող HTTP 403-ը։ Թեստային process-ը հետո կանգնեցվել է։

Docker image build-ը, PostgreSQL/Elasticsearch Docker ինտեգրացիոն պրոֆիլը և երկու իրական browser-ով end-to-end ստուգումը այս փոփոխության ընթացքում չեն գործարկվել։ Frontend-ը չի փոփոխվել։
