import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import rebase.Snowflake
import rebase.controllers.UserController
import rebase.schema.User

@DisplayName("Test User")
class UserTest {
    private var mockedCache: MockCache? = null
    private val prettyPrintJackson: ObjectWriter = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
    private val mockedNewUserClass = UserController.NewUser
    private var firstUser: User? = null
    var secondUser: User? = null

    @Test
    fun `Validate Email`() {
        // Validate email
        if (mockedNewUserClass.validateEmail(firstUser!!.email, null)) {
            return
        } else {
            fail("Default user email (${firstUser!!.email}) should pass.")
        }
    }

    @Test
    fun `Validate Password`() {
        // Validate password (char > 6)
        if (mockedNewUserClass.validatePassword(firstUser!!.password, null)) {
            return
        } else {
            fail("Default user password (${firstUser!!.password}) should pass.")
        }
    }

    @Test
    fun `Validate Username`() {
        // Validate username (username must not already be taken)
        if (mockedNewUserClass.validateUsername(firstUser!!.name, mockedCache!!, null)) {
            return
        } else {
            fail("Default username (${firstUser!!.name}) should not be taken twice.")
        }
    }

    @Nested
    @DisplayName("Test user conflict")
    inner class ConflictingUsers {
        @Test
        fun `Validate Name For Conflicting User`() {
            if (mockedNewUserClass.validateUsername(secondUser!!.name, mockedCache!!, null)) {
                fail("Name validation should fail. It has succeeded on this attempt.")
            } else {
                return
            }
        }

        init {
            mockedCache!!.saveOrReplaceUser(firstUser!!)
        }
    }

    @Nested
    @DisplayName("Test User friendships")
    inner class FriendShips {

        @Test
        fun `Create Request`() {
            firstUser!!.addRequest(secondUser!!.identifier)
            if (firstUser!!.relationships.requests.contains(secondUser!!.identifier)) {
                if (secondUser!!.relationships.pending.contains(firstUser!!.identifier)) {
                    return
                } else {
                    fail("Second user should have first users id in pending")
                }
            } else {
                fail("First user should have user id of second user")
            }
        }

        @Test
        fun `Accept Request`() {
            firstUser!!.addRequest(secondUser!!.identifier)
            secondUser!!.acceptRequest(firstUser!!.identifier)
            if (secondUser!!.relationships.friends.contains(firstUser!!.identifier)) {
                if (firstUser!!.relationships.friends.contains(secondUser!!.identifier)) {
                    return
                } else {
                    fail("First user's friends should contain second user's id")
                }
            } else {
                fail("Second user's friends should contain first user's id")
            }
        }

        @Test
        fun `Deny Request`() {
            firstUser!!.addRequest(secondUser!!.identifier)
            secondUser!!.removePendingFriend(firstUser!!.identifier)
            if (!firstUser!!.relationships.requests.contains(secondUser!!.identifier)) {
                if (!secondUser!!.relationships.pending.contains(firstUser!!.identifier)) {
                    return
                } else {
                    fail("Second user pending still contains first user's id")
                }
            } else {
                fail("First user requests still contain second user's id")
            }
        }

        @Test
        fun `Remove friend`() {
            firstUser?.removeFriend(secondUser!!.identifier)
            if (!firstUser!!.relationships.friends.contains(firstUser!!.identifier)) {
                if (!secondUser!!.relationships.friends.contains(firstUser!!.identifier)) {
                    return
                } else {
                    fail("Second user is still friends with first user")
                }
            } else {
                fail("First user is still friends with second user")
            }
        }

        init {
            mockedCache!!.saveOrReplaceUser(firstUser!!)
            mockedCache!!.saveOrReplaceUser(secondUser!!)
        }
    }

    init {
        println("⚠️ Start cache creation")
        val snowflakeCreator = Snowflake()
        println("✅ Created Snowflake singleton")
        mockedCache = MockCache(snowflakeCreator)
        println("✅ Created mock cache")
        firstUser = User(cache = mockedCache, identifier = snowflakeCreator.nextId())
        println("Created Default User ⬇")
        println(prettyPrintJackson.writeValueAsString(firstUser))
        secondUser = User(
            email = "notavalidemail", password = "badpa", cache = mockedCache, identifier = snowflakeCreator.nextId()
        )
        println("Created second user.. with invalid arguments")
        println(prettyPrintJackson.writeValueAsString(secondUser))

    }
}