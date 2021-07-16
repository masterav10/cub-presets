package org.bytedeco.cuda.presets;

import org.bytedeco.javacpp.annotation.NoException;
import org.bytedeco.javacpp.annotation.Platform;
import org.bytedeco.javacpp.annotation.Properties;
import org.bytedeco.javacpp.tools.Info;
import org.bytedeco.javacpp.tools.InfoMap;
import org.bytedeco.javacpp.tools.InfoMapper;

@Properties(
        inherit = cudart.class, 
        names = {"windows-x86_64"}, 
        target = "org.bytedeco.cuda.cub", 
        global = "org.bytedeco.cuda.global.cub",
        value = {
            @Platform(
                include = {
                    "<cub/device/device_histogram.cuh>", 
                }
            )
        }
)
@NoException
@SuppressWarnings("java:S101")
public class cub implements InfoMapper
{
    @Override
    public void map(InfoMap infoMap)
    {
        infoMap.put(new Info("CUB_NS_PREFIX", "CUB_RUNTIME_FUNCTION").cppTypes().annotations());
        infoMap.put(new Info("cub::DeviceHistogram::HistogramEven<float*,int,float,int>").javaNames("HistogramEven"));
    }
}
