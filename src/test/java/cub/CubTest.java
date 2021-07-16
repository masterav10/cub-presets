package cub;

import java.util.Arrays;

import org.bytedeco.cuda.global.cudart;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.SizeTPointer;
import org.junit.jupiter.api.Test;

public class CubTest
{
    @Test
    public void testCodeInDocs()
    {
        // Declare, allocate, and initialize device-accessible pointers for input
        // samples and
        // output histogram
        int num_samples = 10; // e.g., 10
        float[] samplesArray =
        { 2.2F, 6.0F, 7.1F, 2.9F, 3.5F, 0.3F, 2.9F, 2.0F, 6.1F, 999.5F };
        int num_levels = 7; // e.g., 7 (seven level boundaries for six bins)
        float lower_level = 0.0F; // e.g., 0.0 (lower sample value boundary of lowest bin)
        float upper_level = 12.0F; // e.g., 12.0 (upper sample value boundary of upper bin)

        FloatPointer d_samples = new FloatPointer(samplesArray);
        IntPointer d_histogram = new IntPointer(8);

        // Determine temporary device storage requirements
        Pointer d_temp_storage = new Pointer();
        SizeTPointer temp_storage_bytes = new SizeTPointer(1L);

        Cub.HistogramEven(d_temp_storage, temp_storage_bytes, d_samples, d_histogram, num_levels, lower_level,
                upper_level, num_samples);
        // Allocate temporary storage
        long bytes = temp_storage_bytes.get();
        cudart.cudaMalloc(d_temp_storage, bytes);
        // Compute histograms
        Cub.HistogramEven(d_temp_storage, temp_storage_bytes, d_samples, d_histogram, num_levels, lower_level,
                upper_level, num_samples);

        int[] histogram = new int[8];
        d_histogram.asByteBuffer()
                   .asIntBuffer()
                   .get(histogram);

        System.out.println(Arrays.toString(histogram));

        // d_histogram <-- [1, 0, 5, 0, 3, 0, 0, 0];
    }
}
