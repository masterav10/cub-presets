package thrust;

import static thrust.Thrust.*;

import org.bytedeco.javacpp.IntPointer;
import org.junit.jupiter.api.Test;

import thrust.Thrust.IntDeviceVector;
import thrust.Thrust.IntHostVector;
import thrust.Thrust.IntPlus;

public class JavacppSampleTest
{
    @Test
    public void verifySample()
    {
        // generate 32M random numbers serially
        IntHostVector h_vec = new IntHostVector(32 << 20);
        generate(h_vec.begin(), h_vec.end(), rand());

        // transfer data to the device
        IntDeviceVector d_vec = new IntDeviceVector(h_vec);

        // sort data on the device (846M keys per second on GeForce GTX 480)
        sort(d_vec.begin(), d_vec.end());

        // transfer data back to host
        copy(d_vec.begin(), d_vec.end(), h_vec.begin());

        // compute sum on device
        int x = reduce(d_vec.begin(), d_vec.end(), 0, new IntPlus());
    }
}
