import java.io.IOException;

/**
 * @since v0.1.1
 */

public class XCellProcException extends IOException
{
    XCellProcException() {}

    XCellProcException(String gripe)
    {
        super(gripe);
    }
}
