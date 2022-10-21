import java.io.IOException;

/**
 * @since 0.1.1
 */

public class ParsingException extends IOException
{
    ParsingException() {}
    ParsingException(String gripe) {
        super(gripe);
    }
}
