> Maven մոդուլների, ֆայլերի տեղադրության և գործարկման ընթացիկ հրահանգները՝ [README.md](README.md)։

> Ընթացիկ ճարտարապետական տարբերակի և վերջին թեստերի համար տես `ARCHITECTURE_HY.md`։ Ստորև նախորդ տարբերակների պատմական արդյունքներն են։

> See CHAT_FIX_HY.md for the current chat implementation and validation. Earlier chat descriptions below document the previous version.

> Matching v2-ի նոր վարքը, դադարները, գնահատականը և ընթացիկ ստուգումները՝ [MATCHING_V2_HY.md](MATCHING_V2_HY.md)։ Ստորև նախորդ տարբերակի նկարագրությունն է. matching-ի հակասող կանոնների դեպքում առաջնորդվել v2 ուղեցույցով։

# Validation — Connect-only flow

- Java 17 compilation and executable JAR packaging: PASS (Maven package).
- Backend JUnit / Mockito / H2 / HTTP: 83 tests, 0 failures, 0 errors, 0 skipped.
- Frontend React component/integration tests: 20 passed, 0 failed. API and socket responses are mocked.
- TypeScript noEmit: PASS.
- React production build: PASS, with four existing Hook dependency warnings in Feedbacks, LocationPicker, Users and AdminDashboard.

| Backend suite | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| com.talk_space.ConnectOnlyHttpTest | 10 | 0 | 0 | 0 |
| com.talk_space.FeatureHttpTest | 8 | 0 | 0 | 0 |
| com.talk_space.FeedBackServiceTest | 3 | 0 | 0 | 0 |
| com.talk_space.ImageServicePathTest | 2 | 0 | 0 | 0 |
| com.talk_space.PasswordValidatorTest | 7 | 0 | 0 | 0 |
| com.talk_space.SocialMediaValidatorTest | 10 | 0 | 0 | 0 |
| com.talk_space.TalkSpaceApplicationTests | 1 | 0 | 0 | 0 |
| com.talk_space.UserServiceTest | 3 | 0 | 0 | 0 |
| com.talk_space.architecture.FrontendContractTest | 4 | 0 | 0 | 0 |
| com.talk_space.architecture.LegacyEndpointsTest | 1 | 0 | 0 | 0 |
| com.talk_space.modules.network.api.ConnectionServiceTest | 8 | 0 | 0 | 0 |
| com.talk_space.modules.presence.api.PresenceServiceTest | 2 | 0 | 0 | 0 |
| com.talk_space.modules.discovery.api.MatchingServiceTest | 5 | 0 | 0 | 0 |
| com.talk_space.modules.discovery.internal.matching.SimilarityCalculatorTest | 10 | 0 | 0 | 0 |
| com.talk_space.modules.notifications.api.NotificationServiceTest | 3 | 0 | 0 | 0 |
| com.talk_space.search.UserSearchVisibilityTest | 1 | 0 | 0 | 0 |
| com.talk_space.security.SecureSocketInterceptorTest | 5 | 0 | 0 | 0 |

## Behavior covered

- Full public profile on incoming and outgoing connection responses; no password, PIN or email in the profile DTO.
- Chat send/history/read/unread/list require an accepted connection and the authenticated participant.
- A spoofed sender in a socket payload is ignored; the authenticated principal supplies the sender.
- Direct and legacy socket endpoints require authentication; raw broker queues and public broadcasts are rejected.
- Blocking removes existing access, suppresses discovery in both directions, and protects profile/image/history reads.
- Unblocking only removes the caller’s block and requires fresh connection acceptance before chat.
- Pending/rejected requests remain eligible for matching, and a completed discovery cycle restarts.
- Elasticsearch query exclusions cover both block directions; database visibility is checked again after indexed hits.
- Frontend blocks hide an already open conversation and support unblocking from the private blacklist.
- A single server message is rendered once; messages are not broadcast publicly.

## Not executed here

- The PostgreSQL / Redis / Elasticsearch Testcontainers profile and Docker builds were not executed. Native PostgreSQL concurrency/upsert behavior and real infrastructure must be checked with `mvn -Pintegration verify` in a Docker-enabled environment.
- No live two-browser end-to-end run or visual browser inspection was performed. Manual steps are in CONNECT_ONLY_HY.md.
- These checks do not constitute a dependency vulnerability audit.
