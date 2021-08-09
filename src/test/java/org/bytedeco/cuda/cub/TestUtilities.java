package org.bytedeco.cuda.cub;

public class TestUtilities
{
    public static void checkError(int error)
    {
        if (error != 0)
        {
            throw new IllegalArgumentException(String.format("%d", error));
        }
    }
}
