/**
 * | Besoin                                     | `java.io.File` / anciens outils        | `java.nio.file.Path` + `Files` / moderne                            |
| ------------------------------------------ | -------------------------------------- | ------------------------------------------------------------------- |
| **Représenter un chemin**                  | `File`                                 | `Path`                                                              |
| **Créer un chemin**                        | `new File("data/file.txt")`            | `Path.of("data/file.txt")`                                          |
| **Construire un chemin enfant**            | `new File(parent, child)`              | `parent.resolve(child)`                                             |
| **Convertir File → Path**                  | `file.toPath()`                        | —                                                                   |
| **Vérifier existence**                     | `file.exists()`                        | `Files.exists(path)`                                                |
| **Vérifier fichier**                       | `file.isFile()`                        | `Files.isRegularFile(path)`                                         |
| **Vérifier dossier**                       | `file.isDirectory()`                   | `Files.isDirectory(path)`                                           |
| **Créer fichier**                          | `file.createNewFile()`                 | `Files.createFile(path)`                                            |
| **Créer dossier**                          | `file.mkdir()` / `mkdirs()`            | `Files.createDirectory()` / `createDirectories()`                   |
| **Supprimer fichier/dossier**              | `file.delete()`                        | `Files.delete(path)`                                                |
| **Supprimer sans erreur si absent**        | `delete()` retourne `false`            | `Files.deleteIfExists(path)`                                        |
| **Renommer**                               | `file.renameTo(...)`                   | `Files.move(source, target)`                                        |
| **Déplacer**                               | `renameTo()` peut être utilisé         | `Files.move(...)`                                                   |
| **Copier**                                 | `FileInputStream` / `FileOutputStream` | `Files.copy(...)`                                                   |
| **Lire tout le texte**                     | `FileReader` + `BufferedReader`        | `Files.readString(path)`                                            |
| **Lire toutes les lignes**                 | `BufferedReader.readLine()`            | `Files.readAllLines(path)`                                          |
| **Lire ligne par ligne**                   | `BufferedReader`                       | `Files.newBufferedReader(path)`                                     |
| **Écrire du texte**                        | `FileWriter` + `BufferedWriter`        | `Files.writeString(path, content)`                                  |
| **Écrire plusieurs lignes**                | `BufferedWriter`                       | `Files.write(path, lines)`                                          |
| **Écrire ligne par ligne**                 | `BufferedWriter.write()`               | `Files.newBufferedWriter(path)`                                     |
| **Lire des bytes**                         | `FileInputStream`                      | `Files.readAllBytes(path)`                                          |
| **Écrire des bytes**                       | `FileOutputStream`                     | `Files.write(path, bytes)`                                          |
| **Lister un dossier**                      | `File.list()` / `listFiles()`          | `Files.list(path)`                                                  |
| **Parcourir un dossier récursivement**     | récursion manuelle                     | `Files.walk(path)`                                                  |
| **Taille du fichier**                      | `file.length()`                        | `Files.size(path)`                                                  |
| **Date de modification**                   | `file.lastModified()`                  | `Files.getLastModifiedTime(path)`                                   |
| **Chemin absolu**                          | `file.getAbsolutePath()`               | `path.toAbsolutePath()`                                             |
| **Chemin normalisé**                       | limité                                 | `path.normalize()`                                                  |
| **Nom du fichier**                         | `file.getName()`                       | `path.getFileName()`                                                |
| **Dossier parent**                         | `file.getParentFile()`                 | `path.getParent()`                                                  |
| **Extension**                              | à extraire manuellement                | à extraire manuellement                                             |
| **Permissions**                            | méthodes limitées (`canRead`, etc.)    | `Files.isReadable`, `isWritable`, `isExecutable`                    |
| **Vérifier accès lecture**                 | `file.canRead()`                       | `Files.isReadable(path)`                                            |
| **Vérifier accès écriture**                | `file.canWrite()`                      | `Files.isWritable(path)`                                            |
| **Vérifier accès exécution**               | `file.canExecute()`                    | `Files.isExecutable(path)`                                          |
| **Gestion des erreurs**                    | `IOException`, etc.                    | `IOException`, `NoSuchFileException`, `AccessDeniedException`, etc. |
| **Lecture/écriture automatique fermeture** | `try-with-resources`                   | `try-with-resources` également                                      |
| **Options d'écriture**                     | paramètres de `FileWriter`             | `StandardOpenOption`                                                |
| **Ajouter à la fin**                       | `new FileWriter(file, true)`           | `StandardOpenOption.APPEND`                                         |
| **Écraser le contenu**                     | `FileWriter`                           | `Files.writeString()`                                               |
| **Créer si absent**                        | selon l'outil                          | `CREATE`                                                            |
| **Créer uniquement si absent**             | `createNewFile()`                      | `CREATE_NEW`                                                        |
| **Verrouillage fichier**                   | `FileChannel`                          | `FileChannel`                                                       |

 */
/**                    FILE HANDLING
                         │
        ┌────────────────┼────────────────┐
        │                │                │
     PATHS            FILES            STREAMS
        │                │                │
        │                │                ├── Input
        │                │                │    ├── Reader
        │                │                │    └── InputStream
        │                │                │
        │                │                └── Output
        │                │                     ├── Writer
        │                │                     └── OutputStream
        │                │
        │                ├── create
        │                ├── read
        │                ├── write
        │                ├── copy
        │                ├── move
        │                ├── delete
        │                ├── list
        │                └── metadata
        │
        ├── Path.of()
        ├── resolve()
        ├── getParent()
        ├── getFileName()
        ├── normalize()
        └── toAbsolutePath() */
/**Path
 │
 │ représente "où"
 ▼
data/users.txt


Files
 │
 │ effectue "quoi"
 ▼
create / read / write / delete / copy / move


Reader / Writer
 │
 │ gèrent le flux de données
 ▼
texte


InputStream / OutputStream
 │
 │ gèrent le flux de données
 ▼
bytes */
