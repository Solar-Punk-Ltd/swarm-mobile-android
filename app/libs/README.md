# AAR Library Directory

Place your Swarm library `.aar` file in this directory.

For example:
- `swarmlib.aar`
- `swarm-library-release.aar`

The app is configured to automatically include all `.aar` files from this directory via:
```gradle
implementation fileTree(dir: 'libs', include: ['*.aar'])
```
