# Showcase of using react-native with Om/next

Here's what I used to create this small showcase app:

- Natal to create the project structure with the `--interface om-next` option
- https://github.com/oblador/react-native-vector-icons to use custom icons in the TabBar component
- The react-native implementation of Navigator (not NavigatorIos)

# How to run
1. `natal launch`
2. `natal repl`
3. Uncomment the `(navigator...)` form in `core.cljs`, this will reload the app and the buttons will appear.

Notice that the blue buttons allow to switch between home and settings screens.

# Problems
- Find a way to get the TabBarItems to interact with the navigator (an exception occurs with the current implementation).
The idea is to have something similar to http://richardkho.com/persisting-tabbars-in-react-native.


