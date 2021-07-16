package cub;

import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.SizeTPointer;
import org.bytedeco.javacpp.annotation.ByRef;
import org.bytedeco.javacpp.annotation.Cast;
import org.bytedeco.javacpp.annotation.Namespace;
import org.bytedeco.javacpp.annotation.Platform;

@Platform(include =
{ "<cub/cub.cuh>" })
@Namespace("cub::DeviceHistogram")
public class Cub
{
    static
    {
        Loader.load();

        /**
         * @Cast("cudaStream_t") int stream, boolean debug_synchronous
         */
    }

    public static native @Cast("cudaError_t") int HistogramEven(Pointer d_temp_storage,
            @ByRef SizeTPointer temp_storage_bytes, FloatPointer d_samples, IntPointer d_histogram, int num_levels,
            float lower_level, float upper_level, int num_samples);

}
