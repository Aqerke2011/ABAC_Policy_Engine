# Тест нәтижесі

2026 жылғы 22 қарашада осы ZIP-тегі код JDK 17.0.11, Maven 3.9.9 және JUnit Jupiter 5.11.4 арқылы жиналды. Тексеру командасы — `mvn clean package`.

| Тест класы | Өткен тест | Нені тексерді |
|---|---:|---|
| PolicyEngineTest | 15 | Рөл, әрекет, есеп түрі, құрылғы, орын, 09:00 және 18:00 шекаралары, түн ортасы, қате сұраныс, тыйым басымдығы |
| PolicyLoaderTest | 10 | JSON оқу, қате синтаксис, белгісіз атрибут пен амал, қайталанған ID және кілт, бос шарт, жарамсыз уақыт аралығы |
| MainTest | 4 | Консольдегі нәтиже, сыртқы ереже, қате JSON және аргумент саны |
| **Барлығы** | **29** | **0 сәтсіз, 0 қате, 0 өткізіп жіберілген** |

### Maven шығарған қорытынды

```text
Tests run: 29, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Жиналған JAR-ды іске қосқанда

```text
$ java -jar dist/abac.jar employee corporate office 10:00 report read
Decision: PERMIT
Reason:   MATCHED_PERMIT
Rules:    [allow-staff-report]

$ java -jar dist/abac.jar employee personal office 10:00 report read
Decision: DENY
Reason:   EXPLICIT_DENY
Rules:    [deny-personal]
```

Бір ғана атрибут ауысты. Екінші сұраныста жеке құрылғыны шектейтін ереже орындалды. Мұғалімге VS Code терминалында осы екі нәтижені кезекпен көрсет.
