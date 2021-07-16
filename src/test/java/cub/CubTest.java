package cub;

import java.nio.IntBuffer;
import java.util.Arrays;

import org.bytedeco.cuda.cub.DeviceHistogram;
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
        FloatPointer samplesArray = new FloatPointer(2.2F, 6.0F, 7.1F, 2.9F, 3.5F, 0.3F, 2.9F, 2.0F, 6.1F, 999.5F);
        int num_samples = (int) samplesArray.capacity();

        int num_levels = 7; // e.g., 7 (seven level boundaries for six bins)
        float lower_level = 0.0F; // e.g., 0.0 (lower sample value boundary of lowest bin)
        float upper_level = 12.0F; // e.g., 12.0 (upper sample value boundary of upper bin)

        FloatPointer d_samples = new FloatPointer();
        cudart.cudaMalloc(d_samples, num_samples * Float.BYTES);
        cudart.cudaMemcpy(d_samples, new FloatPointer(samplesArray), num_samples * Float.BYTES,
                cudart.cudaMemcpyHostToDevice);

        int bins = num_levels - 1;
        IntPointer d_histogram = new IntPointer();
        cudart.cudaMalloc(d_histogram, bins * Integer.BYTES);

        // Determine temporary device storage requirements
        Pointer d_temp_storage = new Pointer();
        SizeTPointer temp_storage_bytes = new SizeTPointer(1L);

        DeviceHistogram.HistogramEven(d_temp_storage, temp_storage_bytes, d_samples, d_histogram, num_levels,
                lower_level, upper_level, num_samples);
        // Allocate temporary storage
        long bytes = temp_storage_bytes.get();
        cudart.cudaMalloc(d_temp_storage, bytes);
        // Compute histograms
        DeviceHistogram.HistogramEven(d_temp_storage, temp_storage_bytes, d_samples, d_histogram, num_levels,
                lower_level, upper_level, num_samples);

        IntPointer histogramPtr = new IntPointer(num_levels - 1);

        cudart.cudaMemcpy(histogramPtr, d_histogram, histogramPtr.capacity() * Integer.BYTES,
                cudart.cudaMemcpyDeviceToHost);

        int[] histogram = new int[(int) histogramPtr.capacity()];
        IntBuffer buffer = histogramPtr.asBuffer();
        buffer.get(histogram);

        System.out.println(Arrays.toString(histogram));

        // d_histogram <-- [1, 0, 5, 0, 3, 0, 0, 0];
    }
}
