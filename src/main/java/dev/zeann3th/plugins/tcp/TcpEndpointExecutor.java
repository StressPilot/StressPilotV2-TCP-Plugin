package dev.zeann3th.plugins.tcp;

import dev.zeann3th.stresspilot.dto.endpoint.RequestLogDTO;
import dev.zeann3th.stresspilot.entity.EndpointEntity;
import dev.zeann3th.stresspilot.service.executor.EndpointExecutorService;
import okhttp3.CookieJar;
import org.pf4j.Extension;

import java.io.*;
import java.net.Socket;
import java.util.Map;

@Extension
public class TcpEndpointExecutor implements EndpointExecutorService {

    @Override
    public String getType() {
        return "TCP";
    }

    @Override
    public RequestLogDTO execute(EndpointEntity endpoint, Map<String, Object> environment, CookieJar cookieJar) {
        String host = endpoint.getUrl();
        String[] parts = host.split(":");
        String hostname = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 80;

        long startTime = System.currentTimeMillis();

        try (Socket socket = new Socket(hostname, port);
             OutputStream out = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            socket.setSoTimeout(5000);

            // Send body as raw bytes
            if (endpoint.getBody() != null) {
                out.write(endpoint.getBody().getBytes());
                out.flush();
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }

            return RequestLogDTO.builder()
                    .success(true)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .rawResponse(response.toString())
                    .message("TCP Connection Successful")
                    .build();

        } catch (Exception e) {
            return RequestLogDTO.builder()
                    .success(false)
                    .message("TCP Error: " + e.getMessage())
                    .build();
        }
    }
}