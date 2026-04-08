# Projet Robi - Interpreteur graphique scriptable

## Auteurs

- Robin Tirateau (e22500615)
- Francois (SFanch)
- Rayan (RayL4D)
- Kevin SIDER

---

## Liste des exercices rendus

### Exercice 1 - Animation basique (`exercice1.Exercice1_0`)

Rectangle qui se deplace le long des bords de la fenetre en boucle infinie. La couleur change aleatoirement a chaque tour complet. Utilisation directe de `GRect` et `GSpace` sans parsing.

### Exercice 2.1 - Parsing de S-Expressions (`exercice2.Exercice2_1_0`)

Premiere introduction du `SParser`. Un script comme `(space setColor black) (robi setColor yellow)` est parse en arbre `SNode` puis execute via un dispatch `if/else` sur le nom de la cible et de la commande.

### Exercice 2.2 - Translate et Sleep (`exercice2.Exercice2_2_0`)

Extension de l'exercice 2.1 avec les commandes `translate` et `sleep`. Anime un rectangle rouge en le deplacant avec des pauses. Le dispatch reste en `if/else`.

### Exercice 3 - Pattern Command (`exercice3.Exercice3_0`)

Refactoring complet : les commandes `if/else` sont remplacees par des objets `Command` (interface avec methode `run()`). Quatre classes internes : `SpaceChangeColor`, `RobiChangeColor`, `SpaceSleep`, `RobiTranslate`. La fabrique `getCommandFromExpr` instancie la bonne commande selon la S-Expression.

### Exercice 4.1 - Environnement et References (`exercice4.Exercice4_1_0`)

Introduction de l'architecture definitive :
- `Environment` : registre central de references nommees
- `Reference` : associe un objet recepteur a un dictionnaire de commandes
- `RobiInterpreter` : dispatch dynamique sans `if/else`
- Boucle REPL interactive en console

Commandes disponibles : `setColor`, `sleep`, `translate`.

### Exercice 4.2 - Creation dynamique d'elements (`exercice4.Exercice4_2_0`)

Ajout de la creation a la volee d'elements graphiques. Les classes `Rect`, `Oval`, `Image`, `Label` sont enregistrees dans l'environnement en tant que references vers des `Class<?>`. Commandes ajoutees : `add`, `del`, `new`.

### Exercice 5 - Conteneurs imbriques (`exercice5.Exercice5`)

Support de la notation pointee : `space.r1.img` designe un element `img` contenu dans `r1`, lui-meme contenu dans `space`. La suppression en cascade (`removeWithChildren`) nettoie l'environnement. Commande ajoutee : `setDim`.

### Exercice 6 - Scripts utilisateur (`exercice6.Exercice6`)

Les elements peuvent definir des scripts personnalises via `addScript`. La macro-expansion substitue les parametres formels par les arguments reels lors de l'execution. Commande ajoutee : `clear`.

### Exercice 7 - Interpreteur complet (`exercice7.Exercice7`)

Version finale de l'interpreteur cumulant toutes les fonctionnalites precedentes, avec en plus :
- **Boucles** : `repeat N (...)`, `while condition (...)`
- **Conditions** : `if condition (then) (else)`
- **Variables** : `addVar nom valeur` avec sous-commandes `set`, `add`, `print`, `<`, `>`, `==`
- **Operateurs logiques** : `and`, `or`, `not`

---

## Fonctionnalites supplementaires (hors exercices de base)

### Architecture client/serveur

- **`RobiServer`** : serveur TCP multi-thread qui maintient son propre rendu graphique, accepte les connexions clients et execute les scripts
- **`ClientHandler`** : un thread par client, gere le protocole de communication complet
- **`RobiClient`** : client reseau avec thread d'ecoute separe et file d'attente pour les reponses (timeout 30s)
- **`RobiProtocol`** : constantes du protocole (prefixes `SCRIPT:`, `NODES:`, `ERROR:`, `IMAGE:`, `BOT_TICK:`, marqueur `EOM`)
- **`SNodeSerializer`** : serialisation/deserialisation des arbres `SNode` sans repasser par le parser

### Interface graphique (`RobiClientApp`)

Application Swing complete avec theme sombre :
- Panneau de connexion (host/port) avec indicateur visuel
- Barre d'outils : creation d'elements (Rect, Oval, Label, Images), suppression, clear, fleches directionnelles, color picker, screenshots, sauvegarde/chargement
- Console avec historique des commandes et navigation (haut/bas)
- Raccourcis clavier : Ctrl+Entree pour executer, Alt+Fleches pour deplacer
- Mode connecte (execution via serveur) ou local (execution directe)

### Machines a etats (Robibots)

- **`Robibot`** : element graphique pilote par une machine a etats finis, avec detection de collision AABB (bords et entre bots)
- **`StateMachine`** / **`State`** / **`Transition`** : implementation generique d'automate avec actions d'entree/sortie/do et conditions de transition
- **`RobibotFactory`** : fabrique de bots preconfigures (rebond sur les bords avec changement de couleur, patrouille horizontale)
- **`RobibotManager`** : boucle de ticks a 20 FPS, execution des S-Expressions produites par les automates sur l'EDT, notification des clients connectes
- Placeholders `{element}`, `{dx}`, `{dy}` resolus dynamiquement dans les actions

### Persistance

- **`SceneSaver`** : serialise la scene (space + elements) en JSON
- **`SceneLoader`** : deserialise le JSON en S-Expressions executables pour reconstruire la scene

---

## Elements techniques

### Dispatch dynamique des commandes

Le coeur de l'interpreteur repose sur la classe `Reference` qui associe un objet recepteur (n'importe quel type Java) a un dictionnaire `Map<String, Command>`. Quand une S-Expression `(cible commande args...)` est evaluee, l'interpreteur :
1. Cherche la `Reference` nommee dans l'`Environment`
2. Extrait le nom de la commande (index 1 du `SNode`)
3. Appelle `Command.run(receiver, snode)` sur la commande correspondante

Ce mecanisme evite tout `if/else` ou `switch` et permet d'ajouter de nouvelles commandes sans modifier le code existant.

### Evaluation recursive des arguments

`RobiInterpreter.evaluateArgument()` gere trois cas :
1. **Sous-expression** (SNode avec enfants) : execution recursive, unwrap des `GVar`
2. **Entier** : parse directe
3. **Nom de variable** : lookup dans l'environnement, unwrap si `GVar`
4. **Texte brut** : retourne tel quel (nom de couleur, etc.)

### Protocole reseau

Le protocole est textuel, base sur des prefixes et un marqueur de fin `EOM`. Le thread d'ecoute du client separe les messages `BOT_TICK` (dispatches aux listeners) des reponses normales (mises en `LinkedBlockingQueue` pour les methodes synchrones). Cela evite que les ticks haute frequence bloquent l'envoi de scripts.

### Serialisation des SNode

`SNodeSerializer` serialise et deserialise les arbres `SNode` sans repasser par le `SParser`. Cela permet au client de recevoir les noeuds deja structures et de les executer directement sur son interpreteur local, assurant la coherence entre le rendu serveur et le rendu client.

---

## Bilan critique

### Ce qui fonctionne bien

- **L'architecture Command/Reference/Environment** est propre et extensible. Ajouter une nouvelle commande ne demande qu'une classe implementant `Command` et un appel `addCommand`.
- **La progression exercice par exercice** montre bien l'evolution du code, du dispatch naif (ex. 2) vers l'architecture complete (ex. 7).
- **Le client/serveur** fonctionne reellement : deux rendus (serveur et client) restent synchronises, les bots sont geres cote serveur et repliques cote client via les ticks reseau.
- **L'IHM** est fonctionnelle et utilisable, avec un vrai theme sombre coherent.

### Choix problematiques et limites

- **Parsing JSON naif** : `SceneLoader` utilise des `indexOf`/`substring` au lieu d'une vraie librairie JSON. C'est fragile et casse si le JSON a une structure legerement differente. Avec du recul, utiliser `org.json` ou `Gson` aurait ete bien plus robuste.

- **Typage faible dans l'interpreteur** : `evaluateArgument()` retourne un `Object` avec des casts manuels dans chaque commande. Un systeme de types (meme simple : `RobiValue` avec sous-classes `IntValue`, `StringValue`, `BoolValue`) aurait reduit le risque d'erreurs a l'execution et clarifie les contrats.

- **Gestion d'erreurs minimaliste** : la plupart des erreurs sont imprimees sur `stderr` puis ignorees. Un mecanisme de remontee d'erreur vers l'appelant (exceptions typees ou `Result<T>`) permettrait une meilleure experience utilisateur, surtout cote IHM.

- **Duplication de la configuration des commandes** : le bloc `setupInterpreter` / `setupLocalInterpreter` est repete quasi a l'identique dans `Exercice7`, `RobiServer` et `RobiClientApp`. Une methode factory centralisee aurait evite cette repetition.

- **Couleur a l'entree du bouncing bot** : dans `RobibotFactory.createBouncingBot`, la couleur est fixee au moment de la creation (`randomColor()` appele une seule fois dans `addEntryAction`). La couleur ne change donc pas reellement a chaque rebond car l'action est une chaine figee. Il faudrait un placeholder `{randomColor}` ou une action dynamique.

### Ce que nous aurions fait differemment

- Utiliser une librairie JSON (`Gson` ou `Jackson`) pour la persistance
- Creer un `InterpreterFactory` pour eliminer la duplication du setup
- Introduire un type `RobiValue` au lieu de `Object` pour les valeurs evaluees
- Ajouter des tests unitaires JUnit sur l'interpreteur et les commandes
- Implementer un mecanisme d'undo/redo pour les modifications de scene

---

## Ce qui n'a pas ete fait

- **Tests unitaires** sur le package `core` (seul `SParser` a des tests dans `stree.test`)
- **Fichier de configuration externe** (le port serveur et le tick rate sont en dur dans le code)
- **Undo/redo** des modifications graphiques
- **Execution de scripts depuis un fichier** (mode batch)
- **Gestion des erreurs de script** renvoyee proprement a l'utilisateur dans la console IHM
