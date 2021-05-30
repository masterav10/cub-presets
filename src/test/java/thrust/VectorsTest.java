package thrust;

import org.bytedeco.javacpp.IntPointer;
import org.junit.jupiter.api.Test;

import thrust.Thrust.IntDeviceVector;
import thrust.Thrust.IntHostVector;

public class VectorsTest
{
    private static final void printf(String format, Object... args)
    {
        System.out.println(String.format(format, args));
    }

    @Test
    public void sample1()
    {

        // H has storage for 4 integers
        IntHostVector H = new IntHostVector(4L);

        // initialize individual elements
        IntPointer hostData = H.data();
        hostData.put(0, 14);
        hostData.put(1, 20);
        hostData.put(2, 36);
        hostData.put(3, 46);

        // H.size() returns the size of vector H
        printf("H has size %d", H.size());

        // print contents of H
        for (int i = 0; i < H.size(); i++)
        {
            printf("H[%d] = %d", i, hostData.get(i));
        }

        // resize H
        H.resize(2);

        printf("H now has size %d", H.size());

        // Copy host_vector H to device_vector D
        IntDeviceVector D = new IntDeviceVector();
        D.put(H);
        
        // elements of D can be modified
        IntPointer deviceData = D.data()
                                 .get();

        deviceData.put(0, 99);
        deviceData.put(1, 88);

        // print contents of D
        for (int i = 0; i < D.size(); i++)
        {
            printf("D[%d] = %d", i, deviceData.get(i));
        }

        // H and D are automatically deleted when the function returns
    }
}
