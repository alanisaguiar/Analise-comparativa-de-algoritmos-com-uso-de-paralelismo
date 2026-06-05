import static org.jocl.CL.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_program;

public class ParallelGPU {

    public static long count(
        String[] words,
        String targetWord
    ) throws Exception {

        CL.setExceptionsEnabled(true);

        int[] wordHashes = new int[words.length];

        for (int index = 0; index < words.length; index++) {
            wordHashes[index] = words[index].hashCode();
        }

        int targetHash = targetWord.hashCode();
        int[] results = new int[words.length];

        cl_platform_id platform = findNvidiaPlatform();
        cl_device_id device = findGpuDevice(platform);

        cl_context_properties contextProperties =
            new cl_context_properties();

        contextProperties.addProperty(
            CL_CONTEXT_PLATFORM,
            platform
        );

        cl_context context = clCreateContext(
            contextProperties,
            1,
            new cl_device_id[] { device },
            null,
            null,
            null
        );

        cl_command_queue commandQueue =
            clCreateCommandQueueWithProperties(
                context,
                device,
                null,
                null
            );

        String source = Files.readString(
            Path.of("src/kernels/word_count.cl"),
            StandardCharsets.UTF_8
        );

        cl_program program = clCreateProgramWithSource(
            context,
            1,
            new String[] { source },
            null,
            null
        );

        clBuildProgram(
            program,
            0,
            null,
            null,
            null,
            null
        );

        cl_kernel kernel = clCreateKernel(
            program,
            "count_words",
            null
        );

        cl_mem inputBuffer = clCreateBuffer(
            context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            (long) Sizeof.cl_int * wordHashes.length,
            Pointer.to(wordHashes),
            null
        );

        cl_mem outputBuffer = clCreateBuffer(
            context,
            CL_MEM_WRITE_ONLY,
            (long) Sizeof.cl_int * results.length,
            null,
            null
        );

        clSetKernelArg(
            kernel,
            0,
            Sizeof.cl_mem,
            Pointer.to(inputBuffer)
        );

        clSetKernelArg(
            kernel,
            1,
            Sizeof.cl_int,
            Pointer.to(new int[] { targetHash })
        );

        clSetKernelArg(
            kernel,
            2,
            Sizeof.cl_mem,
            Pointer.to(outputBuffer)
        );

        clSetKernelArg(
            kernel,
            3,
            Sizeof.cl_int,
            Pointer.to(new int[] { words.length })
        );

        long[] globalWorkSize = {
            words.length
        };

        clEnqueueNDRangeKernel(
            commandQueue,
            kernel,
            1,
            null,
            globalWorkSize,
            null,
            0,
            null,
            null
        );

        clFinish(commandQueue);

        clEnqueueReadBuffer(
            commandQueue,
            outputBuffer,
            CL_TRUE,
            0,
            (long) Sizeof.cl_int * results.length,
            Pointer.to(results),
            0,
            null,
            null
        );

        long totalOccurrences = 0;

        for (int result : results) {
            totalOccurrences += result;
        }

        clReleaseMemObject(inputBuffer);
        clReleaseMemObject(outputBuffer);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        return totalOccurrences;
    }

    private static cl_platform_id findNvidiaPlatform() {
        int[] numberOfPlatforms = new int[1];

        clGetPlatformIDs(
            0,
            null,
            numberOfPlatforms
        );

        cl_platform_id[] platforms =
            new cl_platform_id[numberOfPlatforms[0]];

        clGetPlatformIDs(
            platforms.length,
            platforms,
            null
        );

        for (cl_platform_id platform : platforms) {
            String platformName =
                getPlatformName(platform);

            if (
                platformName
                    .toLowerCase()
                    .contains("nvidia")
            ) {
                return platform;
            }
        }

        return platforms[0];
    }

    private static cl_device_id findGpuDevice(
        cl_platform_id platform
    ) {
        int[] numberOfDevices = new int[1];

        clGetDeviceIDs(
            platform,
            CL_DEVICE_TYPE_GPU,
            0,
            null,
            numberOfDevices
        );

        cl_device_id[] devices =
            new cl_device_id[numberOfDevices[0]];

        clGetDeviceIDs(
            platform,
            CL_DEVICE_TYPE_GPU,
            devices.length,
            devices,
            null
        );

        return devices[0];
    }

    private static String getPlatformName(
        cl_platform_id platform
    ) {
        long[] size = new long[1];

        clGetPlatformInfo(
            platform,
            CL_PLATFORM_NAME,
            0,
            null,
            size
        );

        byte[] data = new byte[(int) size[0]];

        clGetPlatformInfo(
            platform,
            CL_PLATFORM_NAME,
            data.length,
            Pointer.to(data),
            null
        );

        return new String(
            data,
            0,
            data.length - 1
        );
    }
}