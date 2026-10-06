package no.nav.grunn.og.hjelpestonad.infotrygd.feed

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [Application::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@ActiveProfiles("test")
class ApplicationTest {
    @Test
    fun `application context loads`() {
    }
}
