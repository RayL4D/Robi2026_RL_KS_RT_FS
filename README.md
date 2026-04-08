# Projet Robi - Interpreteur graphique scriptable

## Auteurs

- Robin Tirateau (e22500615)
- Francois (SFanch)
- Rayan (RayL4D)
- Kevin SIDER

---

## Partie 1 - Langage de script pour animations graphiques

### Exercice 1 - Prise en main de la couche graphique (`exercice1.Exercice1_0`)

Animation d'un rectangle bleu (`robi`) qui se deplace le long des bords interieurs de la fenetre en boucle infinie. Le deplacement s'adapte automatiquement au redimensionnement du space. La couleur change aleatoirement a chaque tour complet.

### Exercice 2 - Premiere version d'un interpreteur de script

#### 2.1 Script de configuration (`exercice2.Exercice2_1_0`)

Premiere introduction du `SParser`. Un script comme `(space setColor black) (robi setColor yellow)` est parse en arbre `SNode` puis execute via un dispatch `if/else` sur le nom de la cible et de la commande.

#### 2.2 Script d'animation (`exercice2.Exercice2_2_0`)

Extension avec les commandes `translate` (deplacement dx, dy) et `sleep` (pause en ms). Anime un rectangle rouge en le deplacant avec des pauses entre chaque mouvement. Le dispatch reste en `if/else`.

### Exercice 3 - Introduction des commandes (`exercice3.Exercice3_0`)

Remplacement des `if/else` par des classes conformes a l'interface `Command` (methode `run()`). Quatre classes internes : `SpaceChangeColor`, `RobiChangeColor`, `SpaceSleep`, `RobiTranslate`. La fabrique `getCommandFromExpr` analyse la S-Expression et retourne l'instance de `Command` correspondante.

### Exercice 4 - Selection et execution des commandes

#### 4.1 Referencement des objets et enregistrement des commandes (`exercice4.Exercice4_1_0`)

Introduction de l'architecture definitive basee sur trois classes :
- **`Environment`** : registre central qui associe des noms a des `Reference` (remplace la double conditionnelle)
- **`Reference`** : associe un objet recepteur (`Object`) a un dictionnaire de commandes nommees (`Map<String, Command>`)
- **`Command`** : interface evoluee avec `run(Reference receiver, SNode method)` qui recoit le recepteur et la S-Expression complete

La fonction `run` n'utilise plus aucune conditionnelle : elle retrouve la `Reference` par nom dans l'`Environment`, qui retrouve la `Command` par selecteur dans ses `primitives`. Boucle REPL interactive en console via `Tools.readKeyboard()`.

Commandes disponibles : `setColor`, `sleep`, `translate`.

#### 4.2 Ajout et suppression dynamique d'elements graphiques (`exercice4.Exercice4_2_0`)

Les classes graphiques (`GRect`, `GOval`, `GImage`, `GString`) sont enregistrees dans l'environnement comme des `Reference` vers des `Class<?>`. La commande `new` utilise la reflexivite (`getDeclaredConstructor().newInstance()`) pour creer les instances. Commandes ajoutees : `add`, `del`, `new`. Chaque element cree est automatiquement configure avec `setColor`, `translate`, `setDim`.

### Exercice 5 - Ajouter des elements a des conteneurs (`exercice5.Exercice5`)

Les `GRect` et `GOval` etant des `GContainer`, ils peuvent contenir d'autres elements. Notation pointee pour eviter les conflits de nom : `space.robi.im` designe l'image `im` dans le rectangle `robi` dans `space`. La suppression en cascade (`removeWithChildren`) nettoie l'environnement et la visualisation graphique. Commande ajoutee : `setDim`.

### Exercice 6 - Creation et execution de scripts (`exercice6.Exercice6`)

Les elements peuvent enregistrer des scripts personnalises via `addScript`. Le premier parametre est toujours `self` (reference du receveur, analogue au `this` de Java). La macro-expansion substitue les parametres formels (`self`, `name`, `w`, `c`, etc.) par les arguments reels lors de l'execution. Commande ajoutee : `clear`.

Exemple : `(space.robi addScript addRect ((self name w c) (self add name (Rect new)) (self.name setColor c) (self.name setDim w w)))` puis `(space.robi addRect mySquare 30 yellow)`.

### Exercice 7 - Expressions, conditionnelles et boucles (`exercice7.Exercice7`)

Version finale de l'interpreteur cumulant toutes les fonctionnalites precedentes, avec en plus :
- **Boucles** : `repeat N (...)`, `while condition (...)`
- **Conditions** : `if condition (then) (else)`
- **Variables** : `addVar nom valeur` avec sous-commandes `set`, `add`, `print`, `<`, `>`, `==`
- **Operateurs logiques** : `and`, `or`, `not`

---

## Partie 2 - Application distribuee et interface graphique

### Exercice 1 (P2) - Application distribuee

Architecture client-serveur TCP ou le serveur parse les S-Expressions et renvoie les `SNode` serialises au client pour execution locale. Le client n'a plus besoin de `SParser`.

**Classes implementees :**

- **`RobiServer`** (`server.RobiServer`) : serveur TCP multi-thread, maintient son propre rendu graphique (GSpace serveur) pour comparaison, accepte les connexions clients et delegue a des `ClientHandler`
- **`ClientHandler`** (`server.ClientHandler`) : un thread par client, recoit les messages, parse et execute cote serveur sur l'EDT via `SwingUtilities.invokeAndWait()`, renvoie les `SNode` serialises
- **`RobiClient`** (`client.RobiClient`) : client reseau avec thread d'ecoute daemon separe (decouplage reception/execution), file d'attente `LinkedBlockingQueue` pour les reponses synchrones (timeout 30s), callbacks via `ConnectionListener` et `BotTickListener`
- **`RobiProtocol`** (`protocol.RobiProtocol`) : constantes du protocole textuel (prefixes `SCRIPT:`, `NODES:`, `ERROR:`, `IMAGE:`, `BOT_TICK:`, `STATE:`, `LOAD:`, marqueur `EOM`)
- **`SNodeSerializer`** (`protocol.SNodeSerializer`) : serialisation/deserialisation custom des arbres `SNode` sans repasser par le `SParser`, permettant au client de fonctionner sans la librairie SParser

**Comparaison rendu serveur/client :** le serveur ouvre sa propre fenetre GSpace et execute chaque script localement. Le client execute les memes `SNode` sur son interpreteur local. Les deux rendus sont donc comparables visuellement.

### Exercice 2 (P2) - Interface graphique (IHM)

#### 2.1 Interface graphique client (`client.gui.RobiClientApp`)

Application Swing complete avec theme sombre (`Theme.java`) :
- **`ConnectionPanel`** : champs host/port, bouton connexion, indicateur visuel (pastille + texte)
- **`ToolbarPanel`** : barre d'outils avec boutons de creation d'elements, suppression, clear, color picker, fleches directionnelles, gestion des bots
- **`ScriptPanel`** : zone de saisie de scripts avec historique des commandes (navigation haut/bas), split pane input/historique
- **`StatusBar`** : barre d'etat en bas de la fenetre
- **`Theme`** : theme sombre coherent (couleurs, polices JetBrains Mono / Segoe UI, bordures)
- **`StyledButton`** : boutons stylises (primary/secondary)
- **`WrapLayout`** : layout manager personnalise pour les boutons de la toolbar

Le mode deconnecte permet l'execution locale sans serveur.

#### 2.2 Manipulation sans ecrire de S-Expressions

L'IHM permet d'envoyer des S-Expressions sans les ecrire textuellement :
- **Boutons de creation** : Rect, Oval, Label (avec saisie du texte), Image (alien.gif, explosion.gif)
- **Selecteur d'element** : combobox pour choisir l'element actif
- **Fleches directionnelles** : boutons + raccourcis Alt+Fleches pour deplacer l'element selectionne
- **Color picker** : combobox de couleurs qui applique `setColor` sur l'element selectionne
- **Bouton suppression/clear** : supprime l'element selectionne ou vide le space
- **Raccourcis clavier** : Ctrl+Entree pour executer le script saisi

### Exercice 3 (P2) - Capture d'ecran

- Le client envoie une requete `SCREENSHOT` au serveur
- Le serveur capture le `GSpace` dans un `BufferedImage`, le peint sur l'EDT via `SwingUtilities.invokeAndWait()`, l'encode en PNG puis en Base64
- Le serveur renvoie `IMAGE:<base64>` au client
- Le client decode le Base64, reconstruit le `BufferedImage` et l'affiche dans une nouvelle fenetre `JFrame` avec un `JScrollPane`

### Exercice 4 (P2) - Robi persistant

**Format choisi : JSON** (sans librairie externe, parsing manuel).

- **`SceneSaver`** (`core.persistence.SceneSaver`) : serialise l'`Environment` et le `GSpace` en JSON. Structure : `{"space": {"color": [r,g,b], "width": w, "height": h}, "elements": [...]}`. Les elements sont tries par profondeur pour garantir l'ordre de recreation.
- **`SceneLoader`** (`core.persistence.SceneLoader`) : deserialise le JSON en generant une liste de S-Expressions (`(space clear)`, `(space add ...)`, `(... setColor ...)`, etc.) qui sont ensuite executees par l'interpreteur. Cette approche evite de dupliquer la logique de creation d'elements.
- **Sauvegarde** : le client demande `SAVE` au serveur qui renvoie le JSON, ou sauvegarde localement en mode deconnecte. L'utilisateur choisit le fichier via `JFileChooser`.
- **Chargement** : le client envoie `LOAD:<json>` au serveur qui genere et execute les S-Expressions, ou charge localement. Les elements sont reintegres dans le selecteur de l'IHM.

### Exercice 5 (P2) - Robibot (machine a etats)

Machine a etats implementee sans librairie externe.

**Classes implementees :**

- **`Robibot`** (`core.statemachine.Robibot`) : encapsule un element graphique (`GBounded`), sa direction (dx, dy), un etat running, et une reference au `GSpace` pour la detection de collision. Methodes `isCollidingWithBorder()` (bords du space) et `isCollidingWith(other)` (intersection AABB entre bots).

- **`StateMachine`** (`core.statemachine.StateMachine`) : contient des `State` nommes, un etat courant et un etat initial. Le cycle de tick evalue les transitions puis execute les actions entry/do/exit. Retourne une liste de S-Expressions a executer.

- **`State`** (`core.statemachine.State`) : etat nomme avec trois listes d'actions (entry, do, exit) sous forme de S-Expressions textuelles. Contient aussi les `Transition` sortantes.

- **`Transition`** (`core.statemachine.Transition`) : etat cible + condition (`BotCondition`, interface fonctionnelle `boolean test(Robibot)`).

- **`RobibotFactory`** (`core.statemachine.RobibotFactory`) : fabrique de bots preconfigures :
  - **Bouncing** : etat "Move" avec action do `({element} translate {dx} {dy})`, transition "Collide" (self-loop) qui detecte la collision avec les bords et inverse la direction sur l'axe concerne, action entry de changement de couleur
  - **Patrol** : variante horizontale (dy=0)

- **`RobibotManager`** (`core.statemachine.RobibotManager`) : gestionnaire centralise (arbitrage serveur) tournant dans un thread daemon. Boucle de ticks a 50ms (20 FPS). A chaque tick : evalue les machines a etats de tous les bots running, resout les placeholders (`{element}`, `{dx}`, `{dy}`), execute les S-Expressions resultantes sur l'EDT, notifie les clients connectes via `BotTickListener` pour synchronisation.

**Synchronisation client/serveur :** le serveur est l'arbitre central. Les commandes produites par les bots sont executees cote serveur puis diffusees aux clients via le prefixe `BOT_TICK:`. Les clients executent ces memes commandes sur leur interpreteur local, assurant la coherence des rendus.

---

## Elements techniques

### Dispatch dynamique des commandes

Le coeur de l'interpreteur repose sur la classe `Reference` qui associe un objet recepteur (n'importe quel type Java) a un dictionnaire `Map<String, Command>`. Quand une S-Expression `(cible commande args...)` est evaluee, l'interpreteur :
1. Cherche la `Reference` nommee dans l'`Environment`
2. Extrait le nom de la commande (index 1 du `SNode`)
3. Appelle `Command.run(receiver, snode)` sur la commande correspondante

Ce mecanisme evite tout `if/else` ou `switch` et permet d'ajouter de nouvelles commandes sans modifier le code existant. Il y a 29 classes `Command` au total.

### Evaluation recursive des arguments

`RobiInterpreter.evaluateArgument()` gere quatre cas :
1. **Sous-expression** (SNode avec enfants) : execution recursive, unwrap des `GVar`
2. **Entier** : parse directe via `Integer.parseInt`
3. **Nom de variable** : lookup dans l'environnement, unwrap si `GVar`
4. **Texte brut** : retourne tel quel (nom de couleur, chemin de fichier, etc.)

### Protocole reseau

Le protocole est textuel, base sur des prefixes et un marqueur de fin `EOM` :
- Client -> Serveur : `SCRIPT:<s-expression>`, `SCREENSHOT`, `SAVE`, `LOAD:<json>`
- Serveur -> Client : `NODES:<serialized>`, `ERROR:<message>`, `IMAGE:<base64>`, `STATE:<json>`, `BOT_TICK:<commands>`

Le thread d'ecoute du client separe les messages `BOT_TICK` (dispatches directement aux listeners) des reponses normales (mises en `LinkedBlockingQueue` pour les methodes synchrones). Cela evite que les ticks haute frequence des bots bloquent l'envoi de scripts.

### Serialisation des SNode

`SNodeSerializer` serialise et deserialise les arbres `SNode` sans repasser par le `SParser`. Cela permet au client de recevoir les noeuds deja structures et de les executer directement sur son interpreteur local, remplissant l'objectif de retirer `SParser` des dependances du client.

---

## Bilan critique

### Ce qui fonctionne bien

- **L'architecture Command/Reference/Environment** est propre et extensible. Ajouter une nouvelle commande ne demande qu'une classe implementant `Command` et un appel `addCommand`.
- **La progression exercice par exercice** montre l'evolution du dispatch naif (ex. 2) vers l'architecture complete (ex. 7).
- **Le client/serveur** fonctionne : deux rendus (serveur et client) restent synchronises, les bots sont geres cote serveur et repliques cote client via les ticks reseau.
- **L'IHM** est fonctionnelle et utilisable, avec un theme sombre coherent et des raccourcis clavier.
- **La machine a etats** est implementee sans librairie externe, avec une architecture generique (State/Transition/StateMachine) reutilisable pour differents comportements.

### Choix problematiques et limites

- **Parsing JSON naif** : `SceneLoader` utilise des `indexOf`/`substring` au lieu d'une vraie librairie JSON. C'est fragile et peut casser si le JSON a une structure legerement differente (espaces, guillemets echappes, imbrications profondes). Avec du recul, utiliser `org.json` ou `Gson` aurait ete bien plus robuste.

- **Typage faible dans l'interpreteur** : `evaluateArgument()` retourne un `Object` avec des casts manuels dans chaque commande. Un systeme de types (meme simple : `RobiValue` avec sous-classes `IntValue`, `StringValue`, `BoolValue`) aurait reduit le risque de `ClassCastException` et clarifie les contrats.

- **Gestion d'erreurs minimaliste** : la plupart des erreurs sont imprimees sur `stderr` puis ignorees. Un mecanisme de remontee d'erreur vers l'appelant (exceptions typees ou `Result<T>`) permettrait une meilleure experience utilisateur, surtout cote IHM ou les erreurs de script ne sont pas toujours visibles.

- **Duplication de la configuration des commandes** : le bloc `setupInterpreter` / `setupLocalInterpreter` est repete quasi a l'identique dans `Exercice7`, `RobiServer` et `RobiClientApp`. Une methode factory centralisee (`InterpreterFactory`) aurait evite cette repetition.

- **Couleur figee au rebond** : dans `RobibotFactory.createBouncingBot`, la couleur de l'action `entryAction` est fixee a la creation (`randomColor()` appele une seule fois). La couleur ne change donc pas reellement a chaque rebond. Il faudrait un placeholder `{randomColor}` ou une action generant la couleur dynamiquement.

### Ce que nous aurions fait differemment

- Utiliser une librairie JSON (`Gson` ou `Jackson`) pour la persistance
- Creer un `InterpreterFactory` pour eliminer la duplication du setup des commandes
- Introduire un type `RobiValue` au lieu de `Object` pour les valeurs evaluees
- Ajouter des tests unitaires JUnit sur l'interpreteur et les commandes du package `core`
- Implementer un mecanisme d'undo/redo pour les modifications de scene
- Ajouter un guide utilisateur integre a l'IHM (tooltips, aide contextuelle)

---

## Ce qui n'a pas ete fait

- **Pipeline d'assurance qualite** (CI/CD avec compilation, tests, couverture, analyse statique) demande en partie 2 section 2.1
- **Tests unitaires** sur le package `core` (seul `SParser` a des tests dans `stree.test`)
- **Guide utilisateur** documente pour l'IHM (demande en partie 2 exercice 2.2)
- **Fichier de configuration externe** (le port serveur et le tick rate sont en dur dans le code)
- **Undo/redo** des modifications graphiques
- **Execution de scripts depuis un fichier** (mode batch)
