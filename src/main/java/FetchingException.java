import java.io.IOException;

/**
 * @since v0.1.1
 */

public class FetchingException extends IOException
{
    FetchingException() {}

    FetchingException(String gripe)
    {
        super(gripe);
    }
}
