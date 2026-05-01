# Dokumentacja ZKP W Projekcie

## 1. Cel dokumentu

Ten dokument zbiera i porządkuje wszystkie miejsca w folderze `source_code`, w których projekt odnosi się do ZKP albo do wyboru metody dowodu. Dokument odpowiada na trzy pytania:

1. Gdzie w projekcie pojawia się ZKP.
2. Jak działa ten element w obecnej implementacji.
3. Z czym jest powiązany w warstwie UI, algorytmu, raportowania, logów, dokumentacji i testów.

Uwaga aktualizacyjna:

- od dodania algorytmu `ZKP` projekt ma już osobny, faktycznie kryptograficzny mechanizm dowodu wiedzy,
- nadal jednak stary wariant `BFT z regulami prywatnymi` traktuje `STARK` i `Bulletproofs` tylko jako profile symulacyjno-opisowe.

## 2. Co zostało dodane do projektu

### 2.1. Nowy algorytm ZKP

Dodano całkowicie nowy algorytm konsensusu o nazwie `ZKP`, który implementuje prawdziwy mechanizm dowodu wiedzy zerowej wiedzy (Zero-Knowledge Proof - ZKP) dla głosowania. Algorytm ten różni się od pozostałych algorytmów w projekcie tym, że zawiera rzeczywistą kryptografię matematyczną, a nie tylko symulację.

**Główne komponenty dodane:**

1. **PedersenVoteProofSystem.java** - Klasa implementująca zobowiązanie Pedersena i dowód Schnorra w transformacji Fiat-Shamir
2. **ZkpConsensusAlgorithm.java** - Główny algorytm konsensusu wykorzystujący ZKP
3. **ZkpSettingsController.java** - Kontroler ustawień dla algorytmu ZKP
4. **zkpSettingsView.fxml** - Widok ustawień dla ZKP
5. **helpZkp.fxml** - Dokumentacja w aplikacji dla algorytmu ZKP
6. **ZkpConsensusAlgorithmTest.java** - Testy jednostkowe dla algorytmu
7. **PedersenVoteProofSystemTest.java** - Testy dla systemu dowodowego

### 2.2. Mechanizm kryptograficzny

Algorytm ZKP wprowadza prawdziwą kryptografię:

- **Zobowiązanie Pedersena**: Każdy węzeł zobowiązuje się do swojego głosu w sposób ukryty
- **Dowód Schnorra NIZK**: Nieinteraktywny dowód zerowej wiedzy w transformacji Fiat-Shamir
- **Weryfikacja**: Każdy głos jest akceptowany tylko jeśli towarzyszy mu prawidłowy dowód

### 2.3. Integracja z istniejącym systemem

- Dodano nowy typ algorytmu `ZKP` do enum `AlgorithmType`
- Zintegrowano z kontrolerem symulacji `SimulationController`
- Dodano obsługę w systemie dokumentacji aplikacji
- Zaimplementowano testy weryfikujące poprawność działania

## 3. Jak działa algorytm ZKP - wyjaśnienie techniczne

### 3.1. Faza przygotowania (inicjalizacja)

1. Każdy węzeł generuje swój prywatny głos (true/false)
2. Dla każdego głosu tworzy **zobowiązanie Pedersena**:
   - Wybiera losową wartość `r` (randomness)
   - Oblicza zobowiązanie: `C = g^v * h^r mod p`
   - Gdzie `v` to głos (0 lub 1), `g` i `h` to generatory grupy
3. Zobowiązanie jest publiczne, ale głos pozostaje ukryty

### 3.2. Faza wysyłania głosów

Dla każdej pary węzłów (nadawca → odbiorca):

1. Nadawca ujawnia swój głos `v`
2. Tworzy **dowód Schnorra NIZK**:
   - Wybiera losowego świadka `w`
   - Oblicza ogłoszenie: `A = h^w mod p`
   - Oblicza wyzwanie: `c = H(C, v, A, senderId)`
   - Oblicza odpowiedź: `z = w + c * r mod q`
3. Wysyła: `(v, A, z)` - głos i dowód
4. Odbiorca weryfikuje dowód:
   - Sprawdza czy `h^z = A * C^c mod p` (dla głosu true)
   - Lub odpowiednio dla głosu false
5. Jeśli dowód jest prawidłowy, głos jest liczony; w przeciwnym razie odrzucany

### 3.3. Faza decyzji

1. Każdy węzeł liczy głosy tylko z prawidłowo zweryfikowanymi dowodami
2. Wymaga quorum (większości) prawidłowych głosów
3. Jeśli nie ma quorum lub remis, węzeł sygnalizuje alarm
4. W przeciwnym razie przyjmuje decyzję większości

### 3.4. Ochrona przed oszustwami

- **Zobowiązanie wiążące**: Nie można zmienić głosu po utworzeniu zobowiązania bez unieważnienia dowodu
- **Ukrycie**: Zobowiązanie nie ujawnia głosu przed fazą wysyłania
- **Zero-knowledge**: Dowód nie ujawnia tajnej wartości `r`

## 4. Jak działa ZKP w prostych słowach (wersja dla "chłopskiego rozumu")

Wyobraź sobie, że głosujesz w tajnych wyborach, ale chcesz udowodnić, że nie zmieniasz zdania w ostatniej chwili.

### Prosta analogia:

1. **Zobowiązanie** = Zaklejasz swój głos w kopercie i pokazujesz wszystkim, że koperta jest "prawdziwa" (ale nie widać głosu w środku)

2. **Dowód** = Gdy otwierasz kopertę, pokazujesz dowód, że to ten sam głos, który był w środku od początku

3. **Weryfikacja** = Inni sprawdzą, czy dowód pasuje do koperty - jeśli tak, głos się liczy

### Co to daje:

- Nie możesz powiedzieć "głosowałem na A", a potem zmienić na "B" bez że ktoś zauważy oszustwo
- Twój głos pozostaje tajny do momentu ujawnienia
- Wszyscy wiedzą, że system jest uczciwy

### W projekcie:

- Każdy "węzeł" to jak wyborca
- ZKP zapewnia, że nikt nie może "przegłosować" po fakcie
- Jeśli ktoś próbuje oszukać, jego głos jest odrzucany
- Konsensus (zgoda) zapada tylko gdy jest dość uczciwych głosów

## 5. Najważniejszy wniosek

Po aktualnym stanie repozytorium są tu dwa różne sposoby występowania ZKP:

1. `BFT z regulami prywatnymi`
   - `STARK` i `Bulletproofs` są tam tylko profilem konfiguracyjnym, opisowym i raportowym.
2. `ZKP`
   - to nowy, osobny algorytm, który zawiera prawdziwy mechanizm kryptograficzny:
   - zobowiązanie Pedersena,
   - nieinteraktywny dowód Schnorra w transformacji Fiat-Shamir,
   - weryfikację, czy ujawniony głos zgadza się z wcześniejszym ukrytym zobowiązaniem.

To bardzo ważne:

- projekt **nadal nie zawiera rzeczywistej implementacji kryptograficznej STARK**,
- projekt **nadal nie zawiera rzeczywistej implementacji kryptograficznej Bulletproofs**,
- ale projekt **zawiera już osobny, prawdziwy mechanizm ZKP w algorytmie `ZKP`**.

W praktyce oznacza to, że w repo współistnieją:

- warstwa konfiguracyjno-opisowa dla `STARK` / `Bulletproofs`,
- oraz realny dowód wiedzy dla zgodności głosu z zobowiązaniem w algorytmie `ZKP`.


## 2. Najwazniejszy wniosek

Po aktualnym stanie repozytorium sa tu dwa rozne sposoby wystepowania ZKP:

1. `BFT z regulami prywatnymi`
   - `STARK` i `Bulletproofs` sa tam tylko profilem konfiguracyjnym, opisowym i raportowym.
2. `ZKP`
   - to nowy, osobny algorytm, ktory zawiera prawdziwy mechanizm kryptograficzny:
   - zobowiazanie Pedersena,
   - nieinteraktywny dowod Schnorra w transformacji Fiat-Shamira,
   - weryfikacje, czy ujawniony glos zgadza sie z wczesniejszym ukrytym zobowiazaniem.

To bardzo wazne:

- projekt **nadal nie zawiera rzeczywistej implementacji kryptograficznej STARK**,
- projekt **nadal nie zawiera rzeczywistej implementacji kryptograficznej Bulletproofs**,
- ale projekt **zawiera juz osobny, prawdziwy mechanizm ZKP w algorytmie `ZKP`**.

W praktyce oznacza to, ze w repo wspolistnieja:

- warstwa konfiguracyjno-opisowa dla `STARK` / `Bulletproofs`,
- oraz realny dowod wiedzy dla zgodnosci glosu z zobowiazaniem w algorytmie `ZKP`.


## 3. Co oznaczaja STARK i Bulletproofs w tym projekcie

### 3.1. STARK

W sensie koncepcyjnym:

- STARK to rodzina transparentnych dowodow z szybka weryfikacja dla duzych obliczen,
- jest kojarzona z dobra skalowalnoscia i odpornoscia post-kwantowa,
- zwykle prowadzi do wiekszego rozmiaru dowodu niz mniejsze systemy nastawione na kompaktowosc.

W tym projekcie `STARK` oznacza:

- preferencje dla szybkiej weryfikacji,
- profil dla duzych partii danych,
- profil odporny kwantowo,
- wiekszy rozmiar dowodu w opisie raportowym.

### 3.2. Bulletproofs

W sensie koncepcyjnym:

- Bulletproofs to krotkie dowody bez ujawniania danych, czesto kojarzone z malym rozmiarem dowodu,
- dobrze pasuja do malych danych i przypadkow, gdzie wazna jest oszczednosc pasma lub pamieci,
- nie sa w tym projekcie traktowane jako odporne kwantowo.

W tym projekcie `Bulletproofs` oznacza:

- preferencje dla bardzo malego dowodu,
- profil dla malych danych,
- profil dla urzadzen lub laczy o ograniczonych zasobach,
- brak odpornosci kwantowej w opisie raportowym.

### 3.3. Co te profile zmieniaja, a czego nie zmieniaja

Wybor `STARK` lub `Bulletproofs` w obecnym stanie projektu:

- zmienia teksty w UI,
- zmienia podpowiedzi dla uzytkownika,
- zmienia pola w `StepReport`,
- zmienia opisy wyswietlane w panelu informacji,
- zmienia interpretacje profilu dowodu w dokumentacji.

Wybor `STARK` lub `Bulletproofs` **nie** zmienia:

- quorum,
- topologii grafu,
- liczby wiadomosci,
- zasad podpisywania,
- warunkow konsensusu,
- logiki decyzji,
- prywatnych regul alarmowych,
- realnej kryptograficznej weryfikacji dowodu ZKP.


## 4. Przeplyw ZKP przez projekt

Przeplyw danych wyglada tak:

`privateBftSettingsView.fxml`
-> `PrivateBftSettingsController`
-> `AlgorithmSettings["proofMethod"]`
-> `SimulationController.initSimulation()`
-> `PrivateRulesBftAlgorithm.loadEnvironment(...)`
-> `fillProofMethodProperties(...)`
-> `StepReport.properties`
-> `PrivateRulesBftInformationEngine`
-> `InformationController`

Rownolegle dokumentacja w aplikacji idzie przez:

`DocumentationController`
-> `helpPrivateBft.fxml`


## 5. Szczegolowy opis dzialania

### 5.1. Warstwa ustawien UI

Pliki:

- `src/main/resources/view/settings/privateBftSettingsView.fxml`
- `src/main/java/com/example/controller/settings/PrivateBftSettingsController.java`

Tutaj pojawia sie pole:

- `Metoda dowodu`

Uzytkownik wybiera jedna z dwoch wartosci:

- `STARK`
- `Bulletproofs`

Kontroler:

- tworzy ustawienie `proofMethod`,
- ustawia wartosc domyslna `STARK`,
- zasila `SettingComboBox<ProofMethodType>`,
- aktualizuje podpowiedz `proofMethodHintLabel`,
- zapisuje wybor do `AlgorithmSettings`.

Powiazanie z ZKP:

- to jest glowny punkt wejscia ZKP do projektu,
- od tego miejsca projekt zaczyna traktowac symulacje jako profil `STARK` lub `Bulletproofs`.

### 5.2. Start symulacji

Pliki:

- `src/main/java/com/example/controller/SimulationController.java`
- `src/main/java/com/example/algorithm/AlgorithmType.java`

`SimulationController` pobiera ustawienia z kontrolera odpowiedzialnego za `PRIVATE_BFT` i przekazuje je do algorytmu podczas `initSimulation()`.

Powiazanie z ZKP:

- `SimulationController` nie interpretuje sam metod dowodu,
- ale przenosi `proofMethod` do instancji algorytmu,
- dodatkowo w `processStep()` zapisuje alarmy zwiazane z jakoscia dowodu lub konfliktem lidera do logow UI.

### 5.3. Rdzen algorytmu

Plik:

- `src/main/java/com/example/algorithm/PrivateRulesBftAlgorithm.java`

To jest najwazniejsze miejsce dla opisu ZKP w projekcie.

Algorytm posiada pole:

- `private ProofMethodType proofMethod = ProofMethodType.STARK;`

Podczas `loadEnvironment(...)`:

- odczytuje `proofMethod` z `AlgorithmSettings`,
- ustawia wartosc domyslna `STARK`, jesli ustawienia jej nie dostarcza.

Jednoczesnie ten sam plik pokazuje, co jest tutaj prawdziwa logika kryptograficzna:

- `initializeKeys()` generuje pary kluczy RSA,
- `signValue(...)` podpisuje wartosc,
- `verifySignature(...)` sprawdza podpis,
- `verifyMessage(...)` akceptuje tylko poprawnie podpisane wiadomosci,
- `evaluateRoundDecision(...)` podejmuje decyzje na podstawie certyfikatu quorum,
- `maybeSendEcho(...)` pilnuje, aby uczciwy wezel podpisal najwyzej jedna wartosc w rundzie.

Powiazanie z ZKP:

- `proofMethod` nie steruje tymi mechanizmami,
- nie ma osobnej klasy `Proof`,
- nie ma generatora witness,
- nie ma obiektow reprezentujacych obwody arytmetyczne,
- nie ma prawdziwej weryfikacji STARK lub Bulletproofs.

Rola `proofMethod` w algorytmie jest raportowa:

- `fillProofMethodProperties(...)` dopisuje do `StepReport` pola:
  - `dowod`
  - `weryfikacja`
  - `rozmiar_dowodu`
  - `odpornosc_kwantowa`
  - `profil_dowodu`

To oznacza, ze algorytm symuluje konsekwencje wyboru rodzaju dowodu na poziomie opisu, a nie na poziomie mechaniki konsensusu.

### 5.4. Prywatne reguly i ALARM

Pliki:

- `src/main/java/com/example/algorithm/PrivateRuleType.java`
- `src/main/java/com/example/algorithm/PrivateRulesBftAlgorithm.java`
- `src/main/java/com/example/controller/SimulationController.java`
- `src/main/resources/view/legendView.fxml`

Prywatne reguly nie buduja dowodu ZKP. Ich rola jest inna:

- oceniaja wiarygodnosc wyniku rundy,
- sprawdzaja quorum,
- sprawdzaja konflikt lidera,
- sprawdzaja przewage podpisow nad przeciwnym kierunkiem,
- moga zglosic `ALARM`.

Powiazanie z ZKP:

- w warstwie UI alarm jest opisywany jako problem z dowodem lub konfliktem lidera,
- ale technicznie alarm wynika z polityk `PrivateRuleType`, a nie z realnego weryfikatora STARK/Bulletproofs,
- to jest warstwa interpretacyjna: projekt laczy "jakosc dowodu" z analiza lokalnych polityk i konfliktu podpisow.

### 5.5. Raport kroku symulacji

Pliki:

- `src/main/java/com/example/algorithm/report/StepReport.java`
- `src/main/java/com/example/algorithm/PrivateRulesBftAlgorithm.java`

`StepReport` jest kontenerem na dane kroku symulacji.

Powiazanie z ZKP:

- ZKP nie ma osobnej struktury danych,
- zamiast tego informacje o dowodzie trafiaja do zwyklego `Map<String, String> properties`,
- dzieki temu mozna je wyswietlic bez zmiany modelu symulacji.

To wazne architektonicznie:

- ZKP jest tu modelowane jako metadane raportowe,
- a nie jako osobny obiekt domenowy.

### 5.6. Warstwa prezentacji informacji

Pliki:

- `src/main/java/com/example/engines/InformationEngineFactory.java`
- `src/main/java/com/example/engines/PrivateRulesBftInformationEngine.java`
- `src/main/java/com/example/controller/InformationController.java`

`InformationEngineFactory` wybiera specjalny silnik dla `PRIVATE_BFT`.

`PrivateRulesBftInformationEngine`:

- pobiera z raportu pola dotyczace dowodu,
- wyswietla `dowod`, `weryfikacja`, `rozmiar dowodu`, `odpornosc kwantowa`, `profil dowodu`,
- dopisuje do opisu kroku tekst zalezny od `STARK` albo `Bulletproofs`.

`InformationController`:

- renderuje te pola w panelu informacji,
- nie rozroznia ZKP jako osobnego typu obiektu,
- po prostu drukuje przekazane wlasciwosci.

Powiazanie z ZKP:

- tu profil dowodu staje sie widoczny dla uzytkownika koncowego,
- to jest glowna warstwa prezentacyjna ZKP w aplikacji.

### 5.7. Wbudowana dokumentacja aplikacji

Pliki:

- `src/main/java/com/example/controller/DocumentationController.java`
- `src/main/resources/view/documentationPages/helpPrivateBft.fxml`

`DocumentationController` mapuje `PRIVATE_BFT` na strone `helpPrivateBft.fxml`.

Sama strona `helpPrivateBft.fxml` opisuje:

- podpisy cyfrowe,
- quorum,
- reguly prywatne,
- profil dowodu,
- krotka charakterystyke STARK,
- krotka charakterystyke Bulletproofs,
- zastrzezenie, ze profil dowodu nie zmienia quorum.

Powiazanie z ZKP:

- to jest wbudowana dokumentacja edukacyjna,
- potwierdza, ze ZKP jest tutaj przede wszystkim warstwa opisowa i koncepcyjna.

### 5.8. Testy

Plik:

- `src/test/java/com/example/algorithm/PrivateRulesBftAlgorithmTest.java`

Najwazniejszy test dla ZKP:

- `chosenProofMethodIsExposedInStepReport()`

Sprawdza on, ze:

- po wybraniu `Bulletproofs`,
- raport kroku zawiera `dowod = Bulletproofs`,
- raport zawiera `odpornosc_kwantowa = nie`.

Powiazanie z ZKP:

- test potwierdza spiecie konfiguracji, algorytmu i raportowania,
- nie testuje prawdziwej matematyki ZKP,
- testuje jedynie warstwe wyboru profilu i jego ekspozycje w systemie.

### 5.9. Istniejaca dokumentacja zmian

Plik:

- `private_bft_nowe_metody_i_poprawki.txt`

To jest wewnetrzny opis zmian, ktory juz obecnie zawiera bardzo wazny wniosek:

- STARK i Bulletproofs sa w projekcie profilem symulacyjnym i raportowym,
- nie sa pelna implementacja kryptograficzna.

Powiazanie z ZKP:

- ten plik jest zgodny z analiza kodu,
- dokument, ktory czytasz teraz, rozszerza go o mape zaleznosci przez cale repozytorium.


## 6. Mapa wszystkich istotnych miejsc powiązanych z ZKP

### 6.1. Pliki bezposrednio implementujace wybor metody dowodu (stary system)

- `src/main/java/com/example/algorithm/ProofMethodType.java`
  - centralna definicja `STARK` i `Bulletproofs`,
  - zawiera nazwy, podpowiedzi UI i profile raportowe.

- `src/main/java/com/example/controller/settings/PrivateBftSettingsController.java`
  - tworzy i zapisuje ustawienie `proofMethod`.

- `src/main/resources/view/settings/privateBftSettingsView.fxml`
  - definiuje pole `Metoda dowodu` i miejsce na opis.

- `src/main/java/com/example/algorithm/PrivateRulesBftAlgorithm.java`
  - pobiera `proofMethod` i wpisuje jego profil do raportu.

### 6.2. Pliki implementujące nowy algorytm ZKP

- `src/main/java/com/example/algorithm/ZkpConsensusAlgorithm.java`
  - główny algorytm konsensusu z prawdziwym ZKP
  - implementuje dwie fazy: wysyłanie z dowodami i podejmowanie decyzji

- `src/main/java/com/example/algorithm/PedersenVoteProofSystem.java`
  - rdzeń kryptograficzny: zobowiązanie Pedersena + dowód Schnorra NIZK
  - bezpieczne parametry grupy (bezpieczna liczba pierwsza)
  - generowanie zobowiązań, tworzenie i weryfikacja dowodów

- `src/main/java/com/example/controller/settings/ZkpSettingsController.java`
  - kontroler ustawień dla algorytmu ZKP
  - obsługuje parametr `f` (maksymalna liczba zdrajców)

- `src/main/resources/view/settings/zkpSettingsView.fxml`
  - interfejs ustawień: pole dla maksymalnej liczby zdrajców
  - podpowiedź o gwarancji ZKP

- `src/main/resources/view/documentationPages/helpZkp.fxml`
  - dokumentacja w aplikacji wyjaśniająca mechanizm ZKP
  - opis modelu krok po kroku

### 6.3. Pliki posrednio zwiazane z ZKP przez raportowanie i UI

- `src/main/java/com/example/algorithm/report/StepReport.java`
  - niesie właściwości dotyczące dowodu jako metadane.

- `src/main/java/com/example/engines/PrivateRulesBftInformationEngine.java`
  - wyświetla profil dowodu i dopisuje opis zależny od wybranej metody.

- `src/main/java/com/example/engines/InformationEngineFactory.java`
  - podłącza odpowiedni silnik informacyjny dla `PRIVATE_BFT`.

- `src/main/java/com/example/controller/InformationController.java`
  - renderuje informacje o dowodzie w panelu informacji.

- `src/main/java/com/example/controller/SimulationController.java`
  - przekazuje ustawienia do algorytmu i zapisuje alarmy dotyczące problemu z dowodem lub konfliktu lidera.

- `src/main/resources/view/legendView.fxml`
  - pokazuje użytkownikowi znaczenie stanu `Alarm: problem z dowodem lub konfliktem lidera`.

### 6.4. Pliki zwiazane z dokumentacja i dostepnoscia funkcji

- `src/main/java/com/example/algorithm/AlgorithmType.java`
  - rejestruje algorytm `PRIVATE_BFT` i nowy `ZKP`.

- `src/main/java/com/example/controller/DocumentationController.java`
  - łączy algorytm z odpowiednią stroną dokumentacji.

- `src/main/resources/view/documentationPages/helpPrivateBft.fxml`
  - zawiera opis profilu dowodu w starej dokumentacji.

- `private_bft_nowe_metody_i_poprawki.txt`
  - tekstowy opis zmian i ograniczeń obecnego modelu.

### 6.5. Pliki testowe

- `src/test/java/com/example/algorithm/PrivateRulesBftAlgorithmTest.java`
  - sprawdza, że wybrany profil dowodu trafia do raportu.

- `src/test/java/com/example/algorithm/ZkpConsensusAlgorithmTest.java`
  - testuje algorytm ZKP: odrzucanie kłamców, akceptacja uczciwych głosów

- `src/test/java/com/example/algorithm/PedersenVoteProofSystemTest.java`
  - testuje kryptografię: prawidłowe dowody przechodzą, zmienione głosy i spreparowane odpowiedzi są odrzucane


## 7. Jak działa "ZKP" w tym projekcie krok po kroku

### 7.1. Wybór algorytmu ZKP

1. Użytkownik wybiera algorytm `ZKP` z listy dostępnych algorytmów
2. W ustawieniach konfiguruje maksymalną liczbę zdrajców `f`
3. System inicjalizuje `ZkpConsensusAlgorithm` z parametrami

### 7.2. Faza przygotowania

1. Dla każdego węzła tworzony jest sekret głosowania:
   - Losowy głos (true/false)
   - Losowa wartość `r` (randomness)
   - Zobowiązanie Pedersena `C = g^vote * h^r mod p`
2. Zobowiązania są przechowywane prywatnie w algorytmie

### 7.3. Faza wysyłania (SEND)

Dla każdej krawędzi w grafie:

1. Nadawca pobiera zobowiązanie i tworzy dowód:
   - `A = h^w mod p` (ogłoszenie)
   - `c = H(C, vote, A, senderId)` (wyzwanie)
   - `z = w + c * r mod q` (odpowiedź)
2. Wysyła głos + dowód do odbiorcy
3. Odbiorca weryfikuje: `h^z ≟ A * C^c mod p`
4. Jeśli dowód poprawny → głos liczony; jeśli nie → alarm i głos odrzucony

### 7.4. Faza decyzji (CHOOSE)

1. Każdy węzeł zlicza zweryfikowane głosy (własny + otrzymane)
2. Sprawdza quorum: `liczba_głosów >= n - f`
3. Sprawdza większość: `supporting != not_supporting`
4. Jeśli warunki spełnione → decyzja większości; w przeciwnym razie → alarm

### 7.5. Raportowanie i alarmy

- Poprawne dowody: zliczane w `poprawne_dowody`
- Błędne dowody: węzły nadawców dodawane do `bledni_nadawcy`
- Alarm gdy: nieprawidłowy dowód, brak quorum, remis

## 8. Co jest tu rzeczywistą logiką kryptograficzną, a co symulacją

### 8.1. Rzeczywista logika kryptograficzna (w ZKP)

**W PedersenVoteProofSystem:**
- Generowanie bezpiecznych parametrów grupy (bezpieczna liczba pierwsza)
- Prawdziwe zobowiązanie Pedersena z ukryciem i wiązaniem
- Dowód Schnorra NIZK z transformacją Fiat-Shamir
- Kryptograficznie bezpieczna weryfikacja dowodów

**W ZkpConsensusAlgorithm:**
- Prawdziwa weryfikacja każdego głosu przed zliczeniem
- Ochrona przed zmianą głosu po zobowiązaniu
- Kryptograficzne gwarancje zero-knowledge

### 8.2. Symulacja / warstwa opisowa (w PRIVATE_BFT)

Symulowane lub opisowe:
- wybór `STARK` vs `Bulletproofs`,
- "profil weryfikacji",
- "rozmiar dowodu",
- "odporność kwantowa",
- "profil dowodu",
- dopiski do opisów kroku i dokumentacji.

### 8.3. Czego brakuje do pełnej integracji ZKP w całym systemie

Dla PRIVATE_BFT nadal brakuje:
- modelu danych dla prawdziwego dowodu,
- generatora witness,
- obwodów lub constraint system,
- realnego prover-a,
- realnego verifier-a,
- serializacji dowodów,
- pomiaru czasu generacji dowodu,
- pomiaru czasu weryfikacji dowodu,
- osobnych testów kryptograficznych dla ZKP.

Ale algorytm ZKP już to wszystko ma!


## 8. Co jest tu realna logika kryptograficzna, a co symulacja

### 8.1. Realna logika w kodzie

Realnie zaimplementowane:

- generowanie kluczy RSA,
- podpisywanie danych,
- weryfikacja podpisow,
- wykrywanie konfliktu lidera,
- zliczanie podpisow dla `true` i `false`,
- decyzja na podstawie quorum,
- prywatne polityki alarmowe.

### 8.2. Symulacja / warstwa opisowa

Symulowane lub opisowe:

- wybor `STARK` vs `Bulletproofs`,
- "profil weryfikacji",
- "rozmiar dowodu",
- "odpornosc kwantowa",
- "profil dowodu",
- dopiski do opisow kroku i dokumentacji.

### 8.3. Czego brakuje do prawdziwej integracji ZKP

Brakuje miedzy innymi:

- modelu danych dla prawdziwego dowodu,
- generatora witness,
- obwodow lub constraint system,
- realnego prover-a,
- realnego verifier-a,
- serializacji dowodow,
- pomiaru czasu generacji dowodu,
- pomiaru czasu weryfikacji dowodu,
- osobnych testow kryptograficznych dla ZKP.


## 9. Powiazania architektoniczne

### 9.1. ZKP a algorytm konsensusu

Powiazanie:

- `proofMethod` jest podpiety do wariantu `PRIVATE_BFT`,
- ale nie zmienia mechaniki konsensusu.

Znaczenie:

- projekt pokazuje, jak mozna dolaczyc semantyke "rodzaju dowodu" do BFT,
- bez przepisywania samej logiki decyzji.

### 9.2. ZKP a podpisy cyfrowe

Powiazanie:

- projekt laczy w jednym scenariuszu profile ZKP i podpisy cyfrowe,
- ale podpisy sa jedynym naprawde wykonywanym mechanizmem kryptograficznym.

Znaczenie:

- obecna implementacja nadaje ZKP role warstwy interpretacyjnej,
- podczas gdy autentycznosc wiadomosci zapewnia RSA.

### 9.3. ZKP a prywatne reguly

Powiazanie:

- reguly `PrivateRuleType` sluza do lokalnej oceny jakosci decyzji,
- `ALARM` jest prezentowany jako problem z dowodem lub konfliktem lidera.

Znaczenie:

- projekt wykorzystuje je jako most miedzy prywatna polityka lokalna a narracja o jakosci dowodu,
- ale nadal nie jest to weryfikator ZKP w sensie kryptograficznym.

### 9.4. ZKP a dokumentacja w aplikacji

Powiazanie:

- algorytm ma przypisana osobna strone dokumentacji,
- dokumentacja w UI i raporty w kroku symulacji mowia tym samym jezykiem profili `STARK` / `Bulletproofs`.

Znaczenie:

- warstwa edukacyjna jest spojna z warstwa kodu,
- uzytkownik widzi ten sam model w ustawieniach, opisach krokow, alarmach i dokumentacji.


## 9. Podsumowanie zmian i obecnego stanu

### 9.1. Co zostało dodane

**Nowy algorytm ZKP:**
- Pełna implementacja kryptograficzna dowodu wiedzy
- Zobowiązanie Pedersena dla ukrycia głosu
- Nieinteraktywny dowód Schnorra w Fiat-Shamir
- Weryfikacja każdego głosu przed zliczeniem
- Ochrona przed zmianą decyzji po zobowiązaniu

**Integracja z systemem:**
- Dodanie do listy algorytmów w `AlgorithmType`
- Ustawienia w UI (`ZkpSettingsController`, `zkpSettingsView.fxml`)
- Dokumentacja w aplikacji (`helpZkp.fxml`)
- Kompletne testy (`ZkpConsensusAlgorithmTest`, `PedersenVoteProofSystemTest`)

### 9.2. Jak to działa w prostych słowach

Wyobraź sobie głosowanie, gdzie:

1. **Przed głosowaniem**: Każdy wkłada głos do "magicznej koperty" - widać, że coś tam jest, ale nie widać co
2. **Podczas głosowania**: Otwiera kopertę i pokazuje dowód, że to ten sam głos co na początku
3. **Weryfikacja**: Inni sprawdzają dowód - jeśli się zgadza, głos się liczy
4. **Oszustwo**: Jeśli ktoś próbuje zmienić głos, dowód nie przejdzie i głos jest odrzucany

### 9.3. Gwarancje kryptograficzne

- **Ukrycie**: Głos jest tajny do momentu ujawnienia
- **Wiązanie**: Nie można zmienić głosu bez unieważnienia dowodu
- **Zero-knowledge**: Dowód nie ujawnia sekretnych wartości
- **Niezaprzeczalność**: Uczciwy dowód potwierdza prawdziwość głosu

## 10. Końcowe podsumowanie

Po przejrzeniu całego `source_code` można jednoznacznie stwierdzić, że:

- ZKP nie występuje już tylko w `BFT z regulami prywatnymi`,
- w `PRIVATE_BFT` nadal jest reprezentowane jako wybór `proofMethod`,
- wybór ten ma dwie wartości: `STARK` i `Bulletproofs`,
- projekt wykorzystuje ten wybór do konfiguracji, opisu i raportowania,
- **dodatkowo projekt ma teraz osobny algorytm `ZKP` z prawdziwym NIZK dla zgodności głosu z zobowiązaniem**.

Najkrótsza poprawna interpretacja techniczna brzmi:

- **`PRIVATE_BFT` pozostaje symulacją z profilami metod dowodu, ale algorytm `ZKP` jest już rzeczywistą implementacją dowodu wiedzy dla głosu związanego zobowiązaniem**.

Najważniejsze miejsca do dalszej pracy, jeśli kiedykolwiek miałoby tu wejść prawdziwe ZKP do PRIVATE_BFT:

- `ProofMethodType.java`
- `PrivateRulesBftAlgorithm.java`
- `StepReport.java`
- `PrivateRulesBftInformationEngine.java`
- testy dla `PrivateRulesBftAlgorithmTest.java`

Najważniejsze miejsca dla nowego, rzeczywistego mechanizmu `ZKP`:

- `ZkpConsensusAlgorithm.java`
- `PedersenVoteProofSystem.java`
- `ZkpInformationEngine.java` (jeśli zostanie dodany)
- `ZkpSettingsController.java`
- `helpZkp.fxml`
