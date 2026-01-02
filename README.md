# Megalodonte Router

Megalodonte Router is a lightweight routing library designed
specifically for **JavaFX desktop applications**.

It was created to solve a common pain point in JavaFX projects:
**navigation and screen management become messy very quickly** when
everything is handled manually with `Stage`, `Scene`, and imperative
navigation logic.

This Router brings a **route-based navigation model**, inspired by web
frameworks, while staying fully compatible with JavaFX and custom UI
frameworks.

------------------------------------------------------------------------

## Motivation

In traditional JavaFX applications:

-   Navigation logic is spread across multiple classes
-   Opening and closing windows requires repetitive boilerplate
-   Passing parameters between screens is awkward
-   There is no concept of dynamic routes (e.g. `/product/123`)

Megalodonte Router introduces:

-   Centralized route definitions
-   Dynamic route parameters
-   Automatic window spawning and closing
-   Decoupled navigation logic
-   Clean and predictable screen lifecycle

The goal is **simplicity, clarity, and control** --- without introducing
heavy frameworks.

------------------------------------------------------------------------

## Key Features

-   Static and dynamic routes (`/products/${id}`)
-   Route parameter extraction
-   Per-route window configuration (size, title)
-   Spawn and close windows programmatically
-   Optional awareness of route parameters via interface
-   Works with plain JavaFX or custom UI layers

------------------------------------------------------------------------

## Defining Routes

Routes are defined in a single place using the `Router.Route` class.

``` java
public class AppRoutes {

    public Router defineRoutes(Stage stage) throws ReflectiveOperationException {

        var routes = Set.of(
            new Router.Route(
                "home",
                router -> new HomeScreen(router),
                new Router.RouteProps(1300, 700, null)
            ),
            new Router.Route(
                "cad-produtos/${id}",
                router -> new ProdutoScreen(router),
                new Router.RouteProps(1500, 900, "Cadastro de produtos")
            ),
            new Router.Route(
                "detail",
                router -> new DetailScreen(router),
                new Router.RouteProps(900, 700, null)
            )
        );

        return new Router(routes, "home", stage);
    }
}
```

### Route Pattern

-   Static route: `home`
-   Dynamic route: `cad-produtos/${id}`

When navigating to:

    cad-produtos/123

The router automatically extracts:

    id = "123"

------------------------------------------------------------------------

## Navigating Between Screens

To open a new route:

``` java
router.spawnWindow("cad-produtos/123");
```

To close the current spawned window and optionally return to another
route:

``` java
router.closeSpawn("home");
```

This makes navigation explicit and predictable.

------------------------------------------------------------------------

## Receiving Route Parameters

Screens that need access to route parameters simply implement
`RouteParamsAware`.

``` java
public class ProdutoScreen implements RouteParamsAware {

    private String id;

    @Override
    public void onRouteParams(Map<String, String> params) {
        this.id = params.get("id");
    }

    public Component render() {
        System.out.println("Product ID: " + id);
        return new Column(...);
    }
}
```

This keeps constructors clean and avoids tight coupling with the router.

------------------------------------------------------------------------

## Error Handling

If a route cannot be resolved, the router throws:

``` java
RouteNotFoundException
```

This helps catch configuration or navigation errors early during
development.

------------------------------------------------------------------------

## Example Use Case

A home screen with cards that navigate to different features:

``` java
new Column(...)
    .c_child(
        new Clickable(icon)
            .onClick(() -> router.spawnWindow("cad-produtos/teste"))
    );
```

Each card controls navigation without knowing anything about stages or
scenes.

------------------------------------------------------------------------

## Design Philosophy

-   No magic
-   No reflection-heavy frameworks
-   Explicit navigation
-   Desktop-first mindset
-   Inspired by web routing, adapted for JavaFX

Megalodonte Router is meant to **empower**, not abstract away
everything.

------------------------------------------------------------------------

## Status

This library is under active development and evolving alongside
real-world desktop applications.

Feedback, ideas, and improvements are welcome.
