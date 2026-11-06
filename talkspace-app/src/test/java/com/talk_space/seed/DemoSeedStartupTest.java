package com.talk_space.seed;

import com.talk_space.modules.users.internal.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties={"app.demo-seed.enabled=true", "spring.datasource.url=jdbc:h2:mem:demo_seed;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"})
@ActiveProfiles("test")
class DemoSeedStartupTest {
    @Autowired UserRepository users;
    @Test @Transactional void generatedUsersRetainEnrichedProfilesAfterServiceExtraction() {
        assertEquals(20,users.count());
        var user=users.findUserByUserName("AramH95").orElseThrow();
        assertNotNull(user.getAboutMe());
        assertEquals(4,user.getHobbies().size());
        assertEquals(3,user.getSpecialities().size());
        assertEquals("Yerevan",user.getLocation().getCity());
    }
}
