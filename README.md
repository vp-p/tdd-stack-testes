# tdd-stack — testes de unidade (JUnit 5 + Mockito)

Atividade **stack-no-tests** do Laboratório de Desenvolvimento de Banco de Dados V (FATEC/SJC).

Objetivo: entregar o projeto disponibilizado com **100% de cobertura de código e de branches**
usando testes automatizados em nível de unidade.

## O que foi testado

A classe `CustomStack<T>` depende de uma `CalculableStrategy<T>`, que é substituída nos testes por
dublês:

- **Mock/Stub (Mockito)**: `@Mock` + `@ExtendWith(MockitoExtension.class)`, com `when(...).thenReturn(...)`,
  `thenAnswer`, `thenThrow`, `verify(...)`, `never()` e `verifyNoInteractions(...)`.
- **Fake**: uma estratégia real e simplificada (`value -> value * 2`) usada em teste parametrizado.

Recursos do JUnit 5 usados: `@BeforeEach`, `@Nested`, `@DisplayName`, `assertAll`, `assertThrows`,
`@ParameterizedTest` com `@CsvSource` e `@ValueSource`.

Cenários cobertos:

| Cenário | Resultado esperado |
| --- | --- |
| Pilha nova | vazia, `size() == 0`, `pop()` lança `StackEmptyException`, `top()` lança `IndexOutOfBoundsException` |
| `push` | guarda o valor **calculado pela estratégia** |
| `push` além do limite (limites 0, 1, 2, 5, 10) | lança `StackFullException` sem chamar a estratégia |
| Estratégia lança `NullPointerException` | exceção é propagada e nada é empilhado |
| `top` | devolve o topo sem remover |
| `pop` | remove em ordem LIFO até esvaziar |
| `pop` seguido de `push` em pilha cheia | volta a aceitar elementos |

## Cobertura

| Classe | Linhas | Branches |
| --- | --- | --- |
| `CustomStack` | 17/17 (100%) | 4/4 (100%) |
| `StackEmptyException` | 1/1 (100%) | — |
| `StackFullException` | 1/1 (100%) | — |

O plugin **JaCoCo** está configurado com a regra `check`: o build falha se a cobertura de linhas ou
de branches ficar abaixo de 100%.

## Como executar

```bash
mvn verify
```

- Resultado dos testes: `target/surefire-reports/`
- Relatório de cobertura: `target/site/jacoco/index.html`
