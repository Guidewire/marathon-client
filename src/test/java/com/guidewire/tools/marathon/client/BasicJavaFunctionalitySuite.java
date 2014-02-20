package com.guidewire.tools.marathon.client;

import org.junit.Assume;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static com.guidewire.tools.marathon.client.ClientScalaTest.*;


@SuppressWarnings("all")
public class BasicJavaFunctionalitySuite {
  private static final String DEFAULT_HOST = ClientDefaults.DEFAULT_HOST();
  private static final int DEFAULT_PORT = ClientDefaults.DEFAULT_PORT();

  private static interface DeferredAction<T> {
    public Future<T> apply();
  }

  private static class DoICare<T> {
    public final boolean doesThisCare;
    public final T value;
    public DoICare(final boolean i_do_care, final T value) {
      this.doesThisCare = i_do_care;
      this.value = value;
    }
  }

  private static <T> DoICare<DeferredAction<T>> doNotCheck(final DeferredAction<T> action) {
    return check(false, action);
  }

  private static <T> DoICare<DeferredAction<T>> check(final DeferredAction<T> action) {
    return check(true, action);
  }

  private static <T> DoICare<DeferredAction<T>> check(final boolean pleaseCheck, final DeferredAction<T> action) {
    return new DoICare<DeferredAction<T>>(pleaseCheck, action);
  }

  @SafeVarargs
  private static <T> List<DoICare<T>> sequential(final DoICare<DeferredAction<T>>...actions) throws InterruptedException, TimeoutException, ExecutionException {
    final ArrayList<DoICare<T>> list = new ArrayList<DoICare<T>>(actions.length);
    for(final DoICare<DeferredAction<T>> care : actions) {
      assertNotNull(care);
      assertNotNull(care.value);

      final T result = care.value.apply().get(4L, TimeUnit.SECONDS);
      assertNotNull(result);

      list.add(new DoICare<T>(care.doesThisCare, result));
    }
    return list;
  }

  private static <T extends ServerResponse> boolean allSuccess(final List<DoICare<T>> results) {
    for(final DoICare<T> result : results)
      if (result.doesThisCare && !result.value.isSuccess()) {
        fail("[CLEANUP REQUIRED] Unable to fully process test on <" + DEFAULT_HOST + ":" + DEFAULT_PORT + "> for " + result.value);
        return false;
      }
    return true;
  }

  private static DeferredAction<AppResponse> deferredAppStart(final String host, final int port, final App app) {
    return new DeferredAction<AppResponse>() {
      public Future<AppResponse> apply() {
        return Marathon4J.apps.start(host, port, app);
      }
    };
  }

  private static DeferredAction<AppResponse> deferredAppScale(final String host, final int port, final String appID, final int instances) {
    return new DeferredAction<AppResponse>() {
      public Future<AppResponse> apply() {
        return Marathon4J.apps.scale(host, port, appID, instances);
      }
    };
  }

  private static DeferredAction<AppResponse> deferredAppDestroy(final String host, final int port, final String appID) {
    return new DeferredAction<AppResponse>() {
      public Future<AppResponse> apply() {
        return Marathon4J.apps.destroy(host, port, appID);
      }
    };
  }

  @Test
  public void javaQueryApps() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));

    final Future<List<App>> future = Marathon4J.apps.query(DEFAULT_HOST, DEFAULT_PORT);
    assertNotNull(future);

    final List<App> apps = future.get();
    for(final App app : apps) {
      assertNotNull(app.id());
      assertNotNull(app.cmd());

      //System.out.println(app.id() + ": " + app.cmd());
    }
  }

  @Test
  public void javaStartApp() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));
    allSuccess(sequential(
        doNotCheck(deferredAppDestroy(DEFAULT_HOST, DEFAULT_PORT, "junit-app-start"))
      ,      check(deferredAppStart(DEFAULT_HOST, DEFAULT_PORT, Apps.create("junit-app-start", "echo junit-app-start", 1, 1.0D, 10.0D, "", new ArrayList<Integer>(), null)))
      ,      check(deferredAppDestroy(DEFAULT_HOST, DEFAULT_PORT, "junit-app-start"))
    ));
  }

  @Test
  public void javaScaleApp() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));
    allSuccess(sequential(
        doNotCheck(deferredAppDestroy(DEFAULT_HOST, DEFAULT_PORT, "junit-app-scale"))
      ,      check(deferredAppStart(DEFAULT_HOST, DEFAULT_PORT, Apps.create("junit-app-scale", "echo junit-app-scale")))
      ,      check(deferredAppScale(DEFAULT_HOST, DEFAULT_PORT, "junit-app-scale", 3))
      ,      check(deferredAppDestroy(DEFAULT_HOST, DEFAULT_PORT, "junit-app-scale"))
    ));
  }

  @Test
  public void javaDestroyApp() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));
    allSuccess(sequential(
        doNotCheck(deferredAppDestroy(DEFAULT_HOST, DEFAULT_PORT, "junit-app-destroy"))
      ,      check(deferredAppStart(DEFAULT_HOST, DEFAULT_PORT, Apps.create("junit-app-destroy", "echo junit-app-destroy")))
      ,      check(deferredAppDestroy(DEFAULT_HOST, DEFAULT_PORT, "junit-app-destroy"))
    ));
  }

  @Test
  public void javaDebugIsLeader() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));

    final Future<Boolean> debug_is_leader_future = Marathon4J.debug.isLeader(DEFAULT_HOST, DEFAULT_PORT);
    assertNotNull(debug_is_leader_future);

    final boolean result = debug_is_leader_future.get();

    //System.out.println("IS LEADER: " + result);
  }

  @Test
  public void javaDebugLeaderUrl() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));

    final Future<String> debug_leader_url_future = Marathon4J.debug.leaderUrl(DEFAULT_HOST, DEFAULT_PORT);
    assertNotNull(debug_leader_url_future);

    final String result = debug_leader_url_future.get();

    //System.out.println("LEADER URL: " + result);
  }

  @Test
  public void javaEndpointsQuery() throws Throwable {
    Assume.assumeTrue(ping(DEFAULT_HOST, DEFAULT_PORT));

    final Future<List<Endpoint>> future = Marathon4J.endpoints.query(DEFAULT_HOST, DEFAULT_PORT);
    assertNotNull(future);

    final List<Endpoint> result = future.get();
    for(final Endpoint e : result) {
      assertNotNull(e);
      //System.out.println(e.toString());
    }
  }
}
