package com.github.kusumotolab.tc2p.service;


import com.github.kusumotolab.tc2p.framework.Controller;
import com.github.kusumotolab.tc2p.framework.Presenter;
import com.github.kusumotolab.tc2p.framework.UseCase;
import com.github.kusumotolab.tc2p.framework.View;

public class ServiceGraph<V extends View, P extends Presenter, U extends UseCase, C extends Controller> {

  private final ViewFactory<V> viewFactory;
  private final PresenterFactory<V, P> presenterFactory;
  private final UseCaseFactory<P, U> useCaseFactory;
  private final ControllerFactory<P, U, C> controllerFactory;

  private ServiceGraph(final ViewFactory<V> viewFactory,
      final PresenterFactory<V, P> presenterFactory, final UseCaseFactory<P, U> useCaseFactory,
      final ControllerFactory<P, U, C> controllerFactory) {
    this.viewFactory = viewFactory;
    this.presenterFactory = presenterFactory;
    this.useCaseFactory = useCaseFactory;
    this.controllerFactory = controllerFactory;
  }

  public static <V extends View> ServiceGraphWithView<V> view(final ViewFactory<V> viewFactory) {
    return new EmptyServiceGraph().view(viewFactory);
  }

  public Controller resolve() {
    final V view = viewFactory.apply();
    final P presenter = presenterFactory.apply(view);
    final U useCase = useCaseFactory.apply(presenter);
    return controllerFactory.apply(presenter, useCase);
  }

  public interface ViewFactory<V extends View> {

    V apply();
  }

  public interface PresenterFactory<V extends View, P extends Presenter> {

    P apply(final V view);
  }

  public interface UseCaseFactory<P extends Presenter, U extends UseCase> {

    U apply(final P p);
  }

  public interface ControllerFactory<P extends Presenter, U extends UseCase, C extends Controller> {

    C apply(final P presenter, final U useCase);
  }

  public static class EmptyServiceGraph {

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

    public <P extends Presenter> ServiceGraphWithPresenter<V, P> presenter(
        final PresenterFactory<V, P> presenterFactory) {
      return new ServiceGraphWithPresenter<>(viewFactory, presenterFactory);
    }
  }

  public static class ServiceGraphWithPresenter<V extends View, P extends Presenter> {

    private final ViewFactory<V> viewFactory;
    private final PresenterFactory<V, P> presenterFactory;

    ServiceGraphWithPresenter(
        final ViewFactory<V> viewFactory,
        final PresenterFactory<V, P> presenterFactory) {
      this.viewFactory = viewFactory;
      this.presenterFactory = presenterFactory;
    }

    public <U extends UseCase> ServiceGraphWithPresenterAndUseCase<V, P, U> useCase(
        final UseCaseFactory<P, U> useCaseFactory) {
      return new ServiceGraphWithPresenterAndUseCase<>(viewFactory, presenterFactory,
          useCaseFactory);
    }
  }

  public static class ServiceGraphWithPresenterAndUseCase<V extends View, P extends Presenter, U extends UseCase> {

    private final ViewFactory<V> viewFactory;
    private final PresenterFactory<V, P> presenterFactory;
    private final UseCaseFactory<P, U> useCaseFactory;

    ServiceGraphWithPresenterAndUseCase(
        final ViewFactory<V> viewFactory,
        final PresenterFactory<V, P> presenterFactory,
        final UseCaseFactory<P, U> useCaseFactory) {
      this.viewFactory = viewFactory;
      this.presenterFactory = presenterFactory;
      this.useCaseFactory = useCaseFactory;
    }

    public <C extends Controller> ServiceGraphController<V, P, U, C> controller(
        final ControllerFactory<P, U, C> controllerFactory) {
      return new ServiceGraphController<>(viewFactory, presenterFactory, useCaseFactory,
          controllerFactory);
    }
  }

  public static class ServiceGraphController<V extends View, P extends Presenter, U extends UseCase, C extends Controller> {

    private final ViewFactory<V> viewFactory;
    private final PresenterFactory<V, P> presenterFactory;
    private final UseCaseFactory<P, U> useCaseFactory;
    private final ControllerFactory<P, U, C> controllerFactory;

    ServiceGraphController(
        final ViewFactory<V> viewFactory,
        final PresenterFactory<V, P> presenterFactory,
        final UseCaseFactory<P, U> useCaseFactory,
        final ControllerFactory<P, U, C> controllerFactory) {
      this.viewFactory = viewFactory;
      this.presenterFactory = presenterFactory;
      this.useCaseFactory = useCaseFactory;
      this.controllerFactory = controllerFactory;
    }

    @SuppressWarnings("unchecked")
    public ServiceGraph resolve() {
      return new ServiceGraph(viewFactory, presenterFactory, useCaseFactory, controllerFactory);
    }
  }
}

