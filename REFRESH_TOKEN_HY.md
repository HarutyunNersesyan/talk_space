> Maven մոդուլների, ֆայլերի տեղադրության և գործարկման ընթացիկ հրահանգները՝ [README.md](README.md)։

> Բազմամոդուլային backend-ի ընթացիկ գործարկումը՝ արմատում `mvnw.cmd clean verify`, ապա `java -jar talkspace-app/target/talkspace-app.jar`։ Ստորև պահպանված են նախորդ փոփոխությունների մանրամասները։

# TalkSpace․ access token + refresh token

## Ինչպես գործարկել

1. Փոխարինեք և վերագործարկեք երկու նախագծերը։ Գործող PostgreSQL բազայի համար նախ պահեք backup-ը։
2. Backend-ի Flyway V4 միգրացիան ստեղծում է refresh_sessions աղյուսակը։ Հին օգտատերերի և հաղորդագրությունների աղյուսակները չի փոխում։
3. Frontend՝ npm ci և npm start։ Backend՝ ./mvnw spring-boot:run կամ Windows-ում mvnw.cmd spring-boot:run։
4. Թարմացումից հետո մեկ անգամ նորից մուտք գործեք, որպեսզի բրաուզերը ստանա refresh cookie-ն։ Նախկինում տրված access token-ների ժամկետը հետադարձ ուժով չի փոխվում։
5. Տեղային միջավայրում օգտագործեք http://localhost:3000 և http://localhost:8080 հասցեները․ localhost-ը և 127.0.0.1-ը մի խառնեք։

## Վարքագիծ

- Access JWT՝ լռելյայն 15 րոպե։ Առկա API-ների Authorization: Bearer ձևաչափը պահպանված է։
- Refresh token՝ պատահական 256-bit գաղտնիք, 30 օր բացարձակ ժամկետով՝ հաշված login-ից։ Refresh-ը այս 30 օրը չի երկարացնում։
- Refresh token-ը միայն talkspace_refresh HttpOnly, SameSite=Strict cookie-ում է՝ /account ուղով։ Այն չի վերադարձվում JSON-ում և չի պահվում localStorage-ում։ Access JWT-ն շարունակում է պահվել localStorage-ում՝ գործող frontend-ի համատեղելիության համար։
- Բազայում refresh token-ի SHA-256 hash-ն է։ Յուրաքանչյուր refresh փոխում է գաղտնիքն ու hash-ը։ Նախորդ refresh token-ն այլևս չի ընդունվում։ Սեսիայի տողի կողպումը թույլ չի տալիս նույն token-ով միաժամանակ երկու հաջող rotation։
- Գաղտնաբառի փոփոխությունից/վերականգնումից, օգտատիրոջ ջնջումից, բլոկավորումից կամ email verification-ի վերացումից հետո refresh-ը մերժվում է։
- Logout-ը POST /account/logout-ով չեղարկում է տվյալ բրաուզերի refresh սեսիան և մաքրում cookie-ն։ Մյուս սարքերը մուտք գործած են մնում։ Արդեն տրված access JWT-ն կարող է գործել մինչև իր 15-րոպեանոց ժամկետի ավարտը։ Եթե սերվերն անհասանելի է, տեղային logout-ը կատարվում է, բայց սերվերային չեղարկումը հնարավոր չէ հաստատել։
- Frontend-ը token-ը թարմացնում է ժամկետի ավարտից առաջ, իսկ 401 ստանալու դեպքում հարցումը կրկնում է առավելագույնը մեկ անգամ։ Refresh հարցումն ինքն իրեն չի կրկնում։
- Միաժամանակյա refresh հարցումները մեկ ներդիրում միավորվում են։ Web Locks աջակցող բրաուզերներում cookie-ի գործողությունները հերթագրվում են նաև ներդիրների միջև։ Առանց Web Locks-ի միաժամանակյա տարբեր ներդիրների rotation-ը կարող է պահանջել նոր login։
- Token-ի թարմացումը վերամիացնում է WebSocket-ը նոր JWT-ով՝ պահպանելով բաց զրույցն ու չուղարկված տեքստը։

## Endpoint-ներ

POST /account/auth — գործող email/password JSON-ը։ Պատասխանը շարունակում է պարունակել status, token, userName, իսկ refresh-ը դրվում է Set-Cookie-ով։
POST /account/refresh — cookie + X-Refresh-Request: true, պատասխան՝ նոր access JWT և նոր cookie։
POST /account/logout — cookie + X-Refresh-Request: true, պատասխան՝ 204 և cookie-ի մաքրում։
Հին GET /account/profile/logout-ը պահպանված է միայն հին frontend-ի HTTP համատեղելիության համար․ refresh սեսիան չեղարկելու համար օգտագործեք նոր POST-ը։

## Կարգավորումներ

ACCESS_TOKEN_TTL_MS=900000 — access token-ի ժամկետը միլիվայրկյաններով։
FRONTEND_ORIGIN=http://localhost:3000 — frontend-ի ճշգրիտ origin-ը՝ առանց վերջի slash-ի։ Օգտագործվում է HTTP CORS-ի, WebSocket-ի և refresh/logout Origin ստուգման համար։
AUTH_COOKIE_SECURE=false — միայն տեղային HTTP աշխատանքի համար։ HTTPS production-ում սահմանեք true։
JWT_SECRET — production-ում սահմանեք առանձին, գաղտնի Base64 բանալի՝ առնվազն 32 պատահական բայթից։ Առանց այս փոփոխականի պահպանված է նախագծի նախկին development բանալին։ Այն production գաղտնիք չէ։

Production-ում frontend-ն ու backend-ը պետք է լինեն նույն site-ի HTTPS հասցեներով, օրինակ app.example.com և api.example.com։ SameSite=Strict cookie-ն նախատեսված չէ լրիվ տարբեր site-երի համար։ Frontend-ի REACT_APP_API_URL-ը պետք է լինի backend-ի հասցեն։

## Ստուգումներ

- Backend՝ 120 թեստ, 0 ձախողում/սխալ։ Ներառում է login cookie, access-ի 15 րոպեանոց ժամկետ, hash պահպանում, rotation, logout, ժամկետանց/բացակայող/անվավեր refresh, գաղտնաբառի փոփոխություն, բլոկավորված հաշիվ, սխալ Origin, custom header և ժամկետանց Authorization-ի դեպքում refresh։
- Frontend՝ 53 թեստ։ Ներառում է միաժամանակյա հարցումների մեկ refresh, 401-ի մեկ retry, logout/refresh մրցակցություն, սկզբնական session restore, այլ սերվերին JWT չուղարկելը և չատի draft-ի պահպանում։
- TypeScript ստուգում՝ անցել է նախագծում նախկինում առկա unused-variable խիստ ստուգումները անջատած։ Production build՝ անցել է նախկին Hook dependency զգուշացումներով։
- Իրական երկու բրաուզերով end-to-end և Docker/PostgreSQL ինտեգրացիոն թեստերը այս միջավայրում չեն գործարկվել։

Ձեռքով ստուգում՝ մուտք գործեք, DevTools Application/Cookies-ում տեսեք HttpOnly talkspace_refresh cookie-ն, Network-ում refresh-ից հետո նոր access token-ը։ Հետո logout արեք և էջը վերաբացեք․ ավտոմատ մուտք չպետք է վերականգնվի։
