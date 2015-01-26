package can.i.has.experiments

import can.i.has.utils.LaTeXCompilerUtils

import groovy.transform.Canonical

@Canonical
class Workspace {
    final File root

    final File resultsDir
    final File renderDir
    final File rawDir
    final File dslDir
    final File buildDir

    Workspace(File root) {
        this.root = root
        resultsDir = new File(root, "results")
        renderDir = new File(root, "render")
        rawDir = new File(root, "raw")
        dslDir = new File(root, "dsl")
        buildDir = new File(root, "build")
        if (!resultsDir.exists()) resultsDir.mkdirs()
        if (!renderDir.exists()) renderDir.mkdirs()
        if (!rawDir.exists()) rawDir.mkdirs()
        if (!dslDir.exists()) dslDir.mkdirs()
        if (!buildDir.exists()) buildDir.mkdirs()
    }


    File resultFile(String... path) {
        resolve(resultsDir, path)
    }

    File renderFile(String... path) {
        resolve(renderDir, path)
    }

    File rawFile(String... path) {
        resolve(rawDir, path)
    }

    File dslFile(String... path){
        resolve(dslDir, path)
    }

    File buildFile(String... path){
        resolve(buildDir, path)
    }

    static File resolve(File root, String[] path) {
        File out = root
        path.each {
            out = new File(out, it)
        }
        out
    }

    void compile(String... path){
        LaTeXCompilerUtils.compile(renderFile(path), buildFile(path.reverse().tail().reverse()))
    }

    static String[] dirPath(String... path){
        path[0..-2]
    }

    static String[] relativePath(File root, File file){
        root.toURI().relativize( file.toURI() ).toString().split(File.separator)
    }

    void compileAll(){
        renderDir.eachFileRecurse { File f ->
            if (f.file)
                LaTeXCompilerUtils.compile(f,
                    buildFile(
                        dirPath(
                            relativePath(
                                renderDir,
                                f
                            )
                        )
                    )
                )
        }
    }

    @Singleton
    static class Manager {

        static protected ThreadLocal<Workspace> activeWorkspace

        static Workspace getActiveWorkspace() {
            def out = activeWorkspace.get()
            assert out != null        // todo: dedicated exception
            out
        }

        static {
            activeWorkspace = new ThreadLocal<>()
            activeWorkspace.set(null)
        }

        public <T> T withWorkspace(Workspace workspace, Closure<T> closure) {
            Workspace oldWorkspace = activeWorkspace.get()
            try {
                activeWorkspace.set(workspace)
                return closure()
            } finally {
                activeWorkspace.set(oldWorkspace)
            }
        }
    }
}