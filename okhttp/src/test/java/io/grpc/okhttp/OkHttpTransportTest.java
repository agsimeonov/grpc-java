/*
 * Copyright 2016, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.okhttp;

import io.grpc.ServerStreamTracer;
import io.grpc.internal.AccessProtectedHack;
import io.grpc.internal.ClientTransportFactory;
import io.grpc.internal.InternalServer;
import io.grpc.internal.ManagedClientTransport;
import io.grpc.internal.testing.AbstractTransportTest;
import io.grpc.netty.NettyServerBuilder;
import java.net.InetSocketAddress;
import java.util.List;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for OkHttp transport. */
@RunWith(JUnit4.class)
public class OkHttpTransportTest extends AbstractTransportTest {
  private ClientTransportFactory clientFactory = OkHttpChannelBuilder
      // Although specified here, address is ignored because we never call build.
      .forAddress("::1", 0)
      .negotiationType(NegotiationType.PLAINTEXT)
      .buildTransportFactory();

  @After
  public void releaseClientFactory() {
    clientFactory.close();
  }

  @Override
  protected InternalServer newServer(List<ServerStreamTracer.Factory> streamTracerFactories) {
    return AccessProtectedHack.serverBuilderBuildTransportServer(
        NettyServerBuilder
          .forPort(0)
          .flowControlWindow(65 * 1024),
        streamTracerFactories);
  }

  @Override
  protected InternalServer newServer(
      InternalServer server, List<ServerStreamTracer.Factory> streamTracerFactories) {
    int port = server.getPort();
    return AccessProtectedHack.serverBuilderBuildTransportServer(
        NettyServerBuilder
            .forPort(port)
            .flowControlWindow(65 * 1024),
        streamTracerFactories);
  }

  @Override
  protected String testAuthority(InternalServer server) {
    return "[::1]:" + server.getPort();
  }

  @Override
  protected ManagedClientTransport newClientTransport(InternalServer server) {
    int port = server.getPort();
    return clientFactory.newClientTransport(
        new InetSocketAddress("::1", port),
        testAuthority(server),
        null /* agent */,
        null /* proxy */);
  }

  // TODO(ejona): Flaky/Broken
  @Test
  @Ignore
  @Override
  public void flowControlPushBack() {}
}
