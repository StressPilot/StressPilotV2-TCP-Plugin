package dev.zeann3th.plugins.tcp;

import dev.zeann3th.stresspilot.dto.endpoint.RequestLogDTO;
import dev.zeann3th.stresspilot.entity.EndpointEntity;
import dev.zeann3th.stresspilot.service.executor.EndpointExecutorService;
import dev.zeann3th.stresspilot.service.executor.EndpointExecutorUtils; // Import static utils
import okhttp3.CookieJar;
import org.pf4j.Extension;

import java.io.*;
import java.net.InetSocketAddress;
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
        // 1. Process variables in URL (host:port) and Body
        String rawUrl = endpoint.getUrl();
        String processedUrl = EndpointExecutorUtils.replaceVariables(rawUrl, environment);

        String rawBody = endpoint.getBody();
        String processedBody = EndpointExecutorUtils.replaceVariables(rawBody, environment);

        // 2. Parse host and port from the processed URL
        String[] parts = processedUrl.split(":");
        String hostname = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 80;

        long startTime = System.currentTimeMillis();

        // 3. Establish connection with timeout handling
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port), 5000);
            socket.setSoTimeout(5000);

            try (OutputStream out = socket.getOutputStream();
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // 4. Send processed body
                if (processedBody != null) {
                    out.write(processedBody.getBytes());
                    out.flush();
                }

                // 5. Read response
                StringBuilder response = new StringBuilder();
                String line;
                while (socket.isConnected() && !socket.isInputShutdown() && (line = in.readLine()) != null) {
                    response.append(line).append("\n");
                    if (!in.ready()) break;
                }

                return RequestLogDTO.builder()
                        .success(true)
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .rawResponse(response.toString())
                        .message("TCP Connection Successful")
                        .build();
            }
        } catch (Exception e) {
            return RequestLogDTO.builder()
                    .success(false)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .message("TCP Error: " + e.getMessage())
                    .build();
        }
    }
}