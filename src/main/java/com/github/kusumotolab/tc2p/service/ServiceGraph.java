package com.github.kusumotolab.tc2p.service;

import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;

public class ServiceGraph<V extends View, P extends Presenter<V>, U extends UseCase<?, V, P>, C extends Controller<V, P, U>> {

  private final ViewFactory<V> viewFactory;
  private final PresenterFactory<V, P> presenterFactory;
  private final UseCaseFactory<V, P, U> useCaseFactory;
  private final ControllerFactory<V, P, U, C> controllerFactory;

  private ServiceGraph(final ViewFactory<V> viewFactory,
      final PresenterFactory<V, P> presenterFactory, final UseCaseFactory<V, P, U> useCaseFactory,
      final ControllerFactory<V, P, U, C> controllerFactory) {
    this.viewFactory = viewFactory;
    this.presenterFactory = presenterFactory;
    this.useCaseFactory = useCaseFactory;
    this.controllerFactory = controllerFactory;
  }

  static <V extends View> ServiceGraphWithView<V> view(final ViewFactory<V> viewFactory) {
    return new EmptyServiceGraph().view(viewFactory);
  }

  C resolve() {
    final V view = viewFactory.apply();
    final P presenter = presenterFactory.apply(view);
    final U useCase = useCaseFactory.apply(presenter);
    return controllerFactory.apply(useCase);
  }

  public interface ViewFactory<V extends View> {

    V apply();
  }

  public interface PresenterFactory<V extends View, P extends Presenter<V>> {

    P apply(final V view);
  }

  public interface UseCaseFactory<V extends View, P extends Presenter<V>, U extends UseCase<?, V, P>> {

    U apply(final P p);
  }

  public interface ControllerFactory<V extends View, P extends Presenter<V>, U extends UseCase<?, V, P>, C extends Controller<V, P, U>> {

    C apply(final U useCase);
  }

  private static class EmptyServiceGraph {

    private <V extends View> ServiceGraphWithView<V> view(final ViewFactory<V> viewFactory) {
      return new ServiceGraphWithView<>(viewFactory);
    }
  }

  public static class ServiceGraphWithView<V extends View> {

    private final ViewFactory<V> viewFactory;

    private ServiceGraphWithView(
        final ViewFactory<V> viewFactory) {
      this.viewFactory = viewFactory;
    }

    <P extends Presenter<V>> ServiceGraphWithPresenter<V, P> presenter(
        final PresenterFactory<V, P> presenterFactory) {
      return new ServiceGraphWithPresenter<>(viewFactory, presenterFactory);
    }
  }

  public static class ServiceGraphWithPresenter<V extends View, P extends Presenter<V>> {

    private final ViewFactory<V> viewFactory;
    private final PresenterFactory<V, P> presenterFactory;

    ServiceGraphWithPresenter(
        final ViewFactory<V> viewFactory,
        final PresenterFactory<V, P> presenterFactory) {
      this.viewFactory = viewFactory;
      this.presenterFactory = presenterFactory;
    }

    <U extends UseCase<?, V, P>> ServiceGraphWithPresenterAndUseCase<V, P, U> useCase(
        final UseCaseFactory<V, P, U> useCaseFactory) {
      return new ServiceGraphWithPresenterAndUseCase<>(viewFactory, presenterFactory,
          useCaseFactory);
    }
  }

  public static class ServiceGraphWithPresenterAndUseCase<V extends View, P extends Presenter<V>, U extends UseCase<?, V, P>> {

    private final ViewFactory<V> viewFactory;
    private final PresenterFactory<V, P> presenterFactory;
    private final UseCaseFactory<V, P, U> useCaseFactory;

    ServiceGraphWithPresenterAndUseCase(
        final ViewFactory<V> viewFactory,
        final PresenterFactory<V, P> presenterFactory,
        final UseCaseFactory<V, P, U> useCaseFactory) {
      this.viewFactory = viewFactory;
      this.presenterFactory = presenterFactory;
      this.useCaseFactory = useCaseFactory;
    }

    <C extends Controller<V, P, U>> ServiceGraphController<V, P, U, C> controller(
        final ControllerFactory<V, P, U, C> controllerFactory) {
      return new ServiceGraphController<>(viewFactory, presenterFactory, useCaseFactory,
          controllerFactory);
    }
  }

  public static class ServiceGraphController<V extends View, P extends Presenter<V>, U extends UseCase<?, V, P>, C extends Controller<V, P, U>> {

    private final ViewFactory<V> viewFactory;
    private final PresenterFactory<V, P> presenterFactory;
    private final UseCaseFactory<V, P, U> useCaseFactory;
    private final ControllerFactory<V, P, U, C> controllerFactory;

    ServiceGraphController(
        final ViewFactory<V> viewFactory,
        final PresenterFactory<V, P> presenterFactory,
        final UseCaseFactory<V, P, U> useCaseFactory,
        final ControllerFactory<V, P, U, C> controllerFactory) {
      this.viewFactory = viewFactory;
      this.presenterFactory = presenterFactory;
      this.useCaseFactory = useCaseFactory;
      this.controllerFactory = controllerFactory;
    }

    public ServiceGraph<V, P, U, C> resolve() {
      return new ServiceGraph<>(viewFactory, presenterFactory, useCaseFactory, controllerFactory);
    }
  }
}

