# PvPPokeGo — Guia Mestre de Continuidade e Registro

> **ARQUIVO CANÔNICO DO PROJETO**
>
> Este documento é a fonte principal de continuidade do PvPPokeGo.
> Toda nova conversa, correção, melhoria, build, teste ou entrega deve começar por aqui.
> Quando houver divergência entre documentos antigos e este guia, primeiro verificar o estado real do GitHub e então atualizar este arquivo.

## 1. Regra permanente de continuidade

**MELHORAR E ORGANIZAR, NUNCA EMPOBRECER.**

Antes de qualquer trabalho:

1. abrir este documento;
2. verificar a branch real em desenvolvimento;
3. verificar o HEAD atual;
4. conferir PRs e workflows relevantes;
5. ler o checklist da versão atual;
6. identificar o último passo realmente concluído;
7. continuar dali, sem reiniciar a análise;
8. preservar código, testes, histórico, documentação e decisões anteriores;
9. não voltar para uma versão antiga apenas porque foi citada em uma conversa;
10. nunca afirmar build/APK concluído sem artefato real e validação correspondente.

Proibido como rotina:
- `git reset --hard` ou equivalente destrutivo sem autorização explícita;
- renomear APK antigo como versão nova;
- marcar item como concluído sem evidência;
- repetir indefinidamente a mesma pesquisa/validação sem novo sinal;
- criar implementação paralela quando já existe módulo que pode ser evoluído.

## 2. Regra obrigatória de documentação

**Toda mudança relevante deve ser registrada neste guia antes de considerar a etapa encerrada.**

Isso inclui:
- correção de bug;
- alteração de UI/HUD;
- mudança de ícone/arte;
- nova feature;
- mudança de lógica matemática;
- mudança de banco/dataset;
- alteração de arquitetura;
- novo teste;
- mudança de workflow/build;
- APK gerado;
- upload para Drive;
- mudança de branch/versão;
- decisão de adiar ou remover algo;
- bloqueio conhecido.

Cada registro deve conter, quando aplicável:

- data;
- versão;
- branch;
- commit;
- problema/objetivo;
- o que foi alterado;
- arquivos principais;
- testes executados;
- resultado;
- status: `CONCLUÍDO`, `PARCIAL`, `BLOQUEADO` ou `ADIADO`;
- pendências;
- próximo passo.

**Não apagar registros antigos.**
Quando algo mudar, adicionar um novo registro explicando a correção/decisão.

## 3. Fonte de verdade atual

### Fonte canônica

GitHub:
https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md

Espelho no Google Drive:
https://docs.google.com/document/d/178P8tWol8mjv_juy5qhyitEvE6yl1fzPnuLWGDV1cXw/edit?usp=drivesdk

**Regra:** o GitHub é a fonte canônica versionada; o Google Drive é um espelho de consulta/continuidade. Quando houver divergência, verificar o estado real do repositório e atualizar os dois.

Projeto: **PvPPokeGo**  
Repositório: https://github.com/LucianoToscano/PvPPokeGo  
Branch em desenvolvimento: `release/0.5.26`  
PR: **#34**  
Pacote: `com.lucianotoscano.pvppokego`  
Versão: `0.5.26`  
versionCode: `31`

Checklist atual:
- `docs/CHECKLIST-PvPPokeGo-0.5.26.md`

Handoff da release:
- `docs/RELEASE-0.5.26-HANDOFF.md`

Pesquisa externa:
- `docs/research/PVP-EXTERNAL-REFERENCES.md`
- `docs/research/pvp-sources.lock.json`

Último checkpoint de lógica validado antes dos commits de documentação:
- `356f35cf11008921f500ad8fffdcfb891dc344fc`

Commits recentes de documentação:
- `8a38570e04d5ab1512167ab90b117637fb1ec423` — checklist 1–80;
- `34f22208f4a27fa84d0b96e96fe7eb29b839418e` — checklist complementar PBE.

> Sempre confirmar o HEAD real da branch antes de trabalhar. Os hashes acima são checkpoints documentados, não substituem a verificação ao vivo.

## 4. Estado do build

Variante prioritária: **Standard**.

Recorder Compat/gravação:
- código preservado;
- etapa adiada por decisão do projeto;
- não deve bloquear a entrega Standard.

Última validação lógica documentada:
- **132 PASS / 0 FAIL** no harness JVM atual;
- validação isolada do `BattleAssistAdvisor`: PASS.

Importante:
- ainda não existe APK legítimo `0.5.26`;
- runs recentes do GitHub Actions terminaram antes de receber runner (`steps: null`);
- nunca renomear o APK 0.5.22 como 0.5.26.

## 5. Bugs prioritários atuais

### 5.1 Ícones dos Pokémon de reserva — PARCIAL

Sintomas observados em aparelho real:
- cards laterais podem ficar em `?` / `IDENTIFICANDO`;
- Pokémon 2/3 podem não exibir a imagem correta;
- tela de troca também pode ficar sem o ícone correto.

Arquivos prioritários:
- `BattleOverlayView.kt`
- `BattleOverlayService.kt`
- `BattleOcrDetector.kt`
- `PokemonIconAtlas.kt`
- `ReserveCardMatcher.kt`
- `Models.kt`

Fallback desejado:
1. espécie/forma exata no atlas;
2. Dex quando não ambíguo;
3. retrato estável da seleção da equipe;
4. retrato/crop do card nativo;
5. placeholder somente em último caso.

Regra:
**não esconder o ícone apenas porque matchup/recomendação ainda está ANALISANDO.**

Status: **PARCIAL — busca de cards ambíguos e fallback do retrato estável corrigidos; validação em APK real pendente**.

### 5.2 Ícone oficial do aplicativo — ABERTO/PARCIAL

Arte oficial aprovada:
Poké Ball com setas vermelho/laranja e azul/ciano em moldura triangular.

A arte já existiu no projeto, mas o aparelho mostrou novamente o ícone genérico do Android.

Não considerar concluído até validar:
- `android:icon`;
- `android:roundIcon`;
- `@mipmap/ic_launcher`;
- `@mipmap/ic_launcher_round`;
- adaptive icon foreground/background;
- launcher Samsung;
- APK realmente instalado.

Status: **PARCIAL / NÃO VALIDADO NO APK FINAL**.

## 6. Motor matemático/preditivo

O projeto já possui base para:
- energia min/likely/max;
- Fast Move hypotheses;
- residual;
- overfarm;
- back-to-back Charged;
- ChargedMovePredictor;
- DamageForecastEngine;
- ShieldDecisionEngine;
- FastMovePhaseTracker;
- TacticalForecastEngine;
- CMP por range;
- Consistency Score;
- recuperação após frames perdidos;
- evidência observada × matemática × inferência.

Regra:
**probabilidade comportamental nunca substitui matemática determinística.**

Documento detalhado no checklist:
- seção PBE em `docs/CHECKLIST-PvPPokeGo-0.5.26.md`.

## 7. Fontes externas já auditadas

Não repetir a pesquisa sem necessidade.

- PvPoke: `pvpoke/pvpoke@f627e89e53c0c7b903fff097df7a0ad0ac95decc`
- GoPvPSim: `mglerner/gopvpsim@0952963f04bbc313c042a422a3d042c066678639`
- PokeMoves: `jamiehumphries/pokemoves@bd9ac2e2bb4df8dd72337d1aab299e04d42a3093`
- PoGO GBL Analyzer: `crsiebler/pogo-gbl-analyzer@d4dd848f29ff6b929eca5efe69b419aeefffef6d`

Uso:
`upstream -> atualização offline -> validação/diff -> assets locais -> engine Kotlin`

Durante a batalha:
`frames/OCR -> eventos -> estado matemático -> predictor -> overlay`

Sem dependência web no hot path.

## 8. Checklist permanente para encerrar qualquer alteração

Antes de dizer que uma mudança terminou:

- [ ] código atual verificado;
- [ ] alteração aplicada;
- [ ] funcionalidades existentes preservadas;
- [ ] teste relevante executado;
- [ ] checklist atualizado;
- [ ] este Guia Mestre atualizado;
- [ ] commit registrado;
- [ ] limitações/pendências registradas;
- [ ] se houve APK: build, Manifest, assets, ícone, assinatura e SHA-256 validados;
- [ ] se houve entrega: GitHub/Drive atualizados e link final registrado.

## 9. Template obrigatório de registro

Copiar este bloco ao final da seção **Registro cronológico**:

```text
### AAAA-MM-DD — Título curto

Versão:
Branch:
Commit:

OBJETIVO/PROBLEMA:
-

ALTERAÇÕES:
-

ARQUIVOS PRINCIPAIS:
-

TESTES:
-

RESULTADO:
-

STATUS:
CONCLUÍDO / PARCIAL / BLOQUEADO / ADIADO

PENDÊNCIAS:
-

PRÓXIMO PASSO:
-
```

## 10. Prompt padrão para iniciar um novo chat

Usar este texto:

```text
CONTINUAÇÃO DO PROJETO PVPPokeGo — NÃO REINICIAR.

Antes de qualquer alteração, leia o Guia Mestre canônico do projeto:
https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md

Depois:
1. verifique a branch/HEAD reais;
2. leia o checklist da versão atual;
3. identifique o último registro cronológico realmente concluído;
4. continue exatamente dali;
5. não repita pesquisas já documentadas;
6. não volte para versões antigas;
7. preserve funcionalidades existentes;
8. registre neste Guia Mestre toda correção, mudança, teste, build e entrega feita nesta conversa.

Regra principal:
MELHORAR E ORGANIZAR, NUNCA EMPOBRECER.

Se houver divergência entre conversa antiga e repositório, o código/estado real deve ser verificado primeiro e a divergência documentada.
```

## 11. Registro cronológico

### 2026-10-07 — Checklist 1–80 transformado em acompanhamento oficial

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commit: `8a38570e04d5ab1512167ab90b117637fb1ec423`

OBJETIVO/PROBLEMA:
- transformar ideias/requisitos em checklist rastreável.

ALTERAÇÕES:
- criado `docs/CHECKLIST-PvPPokeGo-0.5.26.md`;
- 80 itens classificados como feito/parcial/pendente/adiado.

RESULTADO:
- checklist passou a ser referência de planejamento da 0.5.26.

STATUS:
CONCLUÍDO.

### 2026-10-07 — PBE incorporado ao checklist

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commit: `34f22208f4a27fa84d0b96e96fe7eb29b839418e`

OBJETIVO/PROBLEMA:
- incorporar o Predictive Battle Engine ao planejamento sem criar uma engine paralela.

ALTERAÇÕES:
- adicionado checklist PBE com 24 componentes;
- adicionadas prioridades PBE-A até PBE-J;
- formalizadas regras para separar matemática, possibilidade, previsão e comportamento.

RESULTADO:
- PBE documentado como evolução da engine Kotlin existente.

STATUS:
CONCLUÍDO.

### 2026-10-07 — Regra canônica de documentação e continuidade

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commits:
- `13650e1d1ac5190078aedd6433d284d940173d16` — criação do Guia Mestre;
- `f2b20b8ad84521785a3d9301a2cbbde59d4735e1` — START_HERE redirecionado;
- `0f0288ea7ecca3f927c2bf4646bfa4b661526597` — handoff 0.5.20 arquivado;
- `5bd0cdf45aec1d80140f889ac27776c6abff9f28` — handoff atual redirecionado.

OBJETIVO/PROBLEMA:
- evitar perda de contexto entre chats, duplicação de trabalho e decisões contraditórias.

ALTERAÇÕES:
- criado este Guia Mestre canônico;
- definido protocolo obrigatório de atualização;
- definido template de registro;
- definido prompt padrão para novo chat;
- `START_HERE_NEW_CHAT.md` agora aponta para este documento;
- o handoff antigo da 0.5.20 foi preservado em `docs/archive/CURRENT_DEVELOPMENT_HANDOFF-0.5.20.md`;
- `docs/CURRENT_DEVELOPMENT_HANDOFF.md` agora aponta para o Guia Mestre;
- criado espelho no Google Drive dentro da pasta oficial do projeto.

GOOGLE DRIVE:
- Documento: `PvPPokeGo — GUIA MESTRE DE CONTINUIDADE E REGISTRO`
- ID: `178P8tWol8mjv_juy5qhyitEvE6yl1fzPnuLWGDV1cXw`
- Link: https://docs.google.com/document/d/178P8tWol8mjv_juy5qhyitEvE6yl1fzPnuLWGDV1cXw/edit?usp=drivesdk

RESULTADO:
- existe uma referência principal versionada no GitHub e um espelho persistente no Drive;
- novos chats devem começar pelo Guia Mestre, não por resumos antigos.

STATUS:
CONCLUÍDO.

### 2026-10-07 — Reservas: continuar busca ambígua e reutilizar retrato estável

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commit de código: `fd66d553fc37148c6ab53d96c040b047e01c3bb4`  
HEAD de entrada conferido: `fdd78ab2998e1ed9ead6d55cf71f436763add5ba`

OBJETIVO/PROBLEMA:
- Retomar o bug prioritário 5.1 a partir do estado real, após o último registro de continuidade concluído.
- A leitura dos dois CPs encerrava a busca visual mesmo com associação ambígua/sem correspondência.
- O fallback de retrato da equipe exigia confirmação da posição nativa, embora o retrato já pertencesse ao slot estável.

ALTERAÇÕES:
- ReserveScanPolicy exige confirmação do mapeamento de cada card para interromper a busca; BattleEngine fornece o resultado do matcher existente.
- BattleOverlayView permite o retrato do slot estável mesmo enquanto a posição nativa está pendente, mantendo atlas exato/Dex como prioridade.
- Preservado o requisito de mapeamento confirmado para promover crops nativos ao cache da equipe e posicionar indicadores no seletor de troca.
- Acrescentados testes de CP duplicado com formas do mesmo Dex, CP sem correspondência e segundo mapeamento ausente.
- Checklist atualizado sem promover reconhecimento visual a concluído; espelho do Guia Mestre no Drive sincronizado.

ARQUIVOS PRINCIPAIS:
- app/src/main/java/com/lucianotoscano/pvppokego/engine/ReserveScanPolicy.kt
- app/src/main/java/com/lucianotoscano/pvppokego/engine/BattleEngine.kt
- app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt
- app/src/test/java/com/lucianotoscano/pvppokego/engine/ReserveScanPolicyTest.kt
- docs/CHECKLIST-PvPPokeGo-0.5.26.md
- docs/PROJECT_CONTINUITY_GUIDE.md

TESTES:
- Kotlin/JVM + JUnit 4.13.2 reais: ReserveScanPolicyTest (8) e ReserveCardMatcherTest (10), **18 PASS / 0 FAIL**.
- Suíte histórica de 132 testes não reexecutada nesta sessão.
- Compilação completa Android, renderização de Bitmap/Canvas e APK instalado não validados nesta sessão.
- PR #34 conferido aberto/draft na branch correta.
- Run do HEAD de entrada: 37697150072, job 113051559680, failure com steps=[], runner_id=0; nenhum comando de build executado.
- Ambiente desta sessão possui JDK/Kotlin no Android Studio e SDK android-37.0; isso corrige apenas a descrição do ambiente atual, sem invalidar o bloqueio histórico.
- Git local sem remote-https; download público do repositório retornou 404. Fontes relevantes obtidas pelo conector autenticado, fixadas no HEAD de entrada; nenhuma cópia parcial foi apresentada como checkout completo.
- Leitura/escrita do espelho feita pelo conector nativo: o helper de leitura em arquivo rejeitou o caminho absoluto Windows. Estrutura de uma aba inspecionada e preservada.

RESULTADO:
- Corrigidas duas condições que impediam a recuperação/exibição das reservas; lógica de identificação existente preservada.
- Nenhum APK gerado; ícone do launcher não alterado.

STATUS:
PARCIAL — correção de código e testes direcionados concluídos; aceite visual depende de APK real.

PENDÊNCIAS:
- Build/test Android Standard e validação em aparelho: atlas presente/ausente, CP duplicado, início no meio da batalha, troca/desmaio e seletor de troca.
- Validar ícone oficial instalado e launcher Samsung.
- Medir custo das novas tentativas de busca quando a evidência continuar ambígua; intervalo de OCR existente preservado.

PRÓXIMO PASSO:
- Executar o build Standard em ambiente com acesso ao repositório/dependências e runner funcional; validar os cenários visuais acima antes de encerrar o bug 5.1.
- Depois retomar replay/fixtures e a ordem PBE já documentada, sem reiniciar pesquisa externa.

### 2026-10-07 — Verificação de continuidade e referências do launcher

Versão: 0.5.26  
Branch: `release/0.5.26`  
HEAD verificado antes deste registro: `4b3932d18e9574977f7d5db9eaf1f17aa9273359`  
PR: #34 (aberto, draft)

OBJETIVO/PROBLEMA:
- Retomar do Guia Mestre sem reiniciar nem descartar trabalho; conferir pendências de APK Standard, reservas e launcher.

VERIFICAÇÕES:
- Guia Mestre, checklist 0.5.26 e handoff lidos pelo conector GitHub autenticado.
- Confirmada branch release/0.5.26 e PR #34 aberto/draft.
- Conferido `app/src/main/AndroidManifest.xml`: `android:icon=@mipmap/ic_launcher` e `android:roundIcon=@mipmap/ic_launcher_round` declarados.
- Conferidos `mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml`: ambos apontam para `@drawable/pvppokego_launcher_foreground` e `@color/pvppokego_icon_background`.
- O registro anterior de reservas (`fd66d553`, 18 PASS / 0 FAIL) permanece referência; nenhuma suíte foi reexecutada nesta verificação.

ALTERAÇÕES:
- Somente este registro cronológico; nenhuma alteração em código, assets, configuração de build ou checklist.

TESTES:
- Inspeção estática das referências de Manifest e adaptive icons; nenhum build Android ou teste em aparelho nesta etapa.

RESULTADO:
- Referências do ícone presentes nos arquivos conferidos, sem comprovação da arte visual efetivamente empacotada/instalada.
- Não há APK 0.5.26 validado nesta etapa. Bugs 5.1 e 5.2 permanecem PARCIAIS.

STATUS:
PARCIAL.

PENDÊNCIAS:
- Validar existência e conteúdo visual do asset final, empacotamento, APK Standard e launcher Samsung.
- Executar QA real dos cards de reserva e seletor de troca após build.

PRÓXIMO PASSO:
- Retomar build Standard em ambiente funcional, validar assets/Manifest e testar reservas em aparelho, preservando a implementação atual.

### 2026-10-07 — Fallback de ícone condicionado à correspondência do card

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commit de código: `8dbf9f47e1f35a4b9ff8dc5f1da6141cdafda8bb`

OBJETIVO/PROBLEMA:
- Impedir que o retrato recortado de outro Pokémon apareça no HUD ao reordenar os cards de reserva durante troca/continuidade.

ALTERAÇÕES:
- `BattleOverlayView.drawReservePokemonIcon`: atlas exato/Dex segue prioritário; retrato estável do slot segue disponível; crop temporário indexado pelo card só aparece quando `cardMappingConfirmed` é verdadeiro.
- A política de identificação e o armazenamento de energia não foram alterados.

ARQUIVOS PRINCIPAIS:
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt`

TESTES:
- Inspeção estática do trecho antes da mudança; substituição de trecho exato efetuada no GitHub.
- Build Android e testes de interface física NÃO executados. O run anterior `37701733198` falhou com `steps=null`, antes do runner executar comandos.
- Sem APK produzido; ambiente desta sessão não tem checkout completo autenticado, Android SDK ou Gradle acessíveis; clone HTTPS não resolve `github.com`.

RESULTADO:
- Fallback de crop nativo agora exige mapeamento confirmado, evitando um ícone de espécie errada quando o card ainda é ambíguo.
- Não resolve sozinho placeholders onde espécie e retrato permanecem desconhecidos; validação em aparelho continua obrigatória.

STATUS:
PARCIAL.

PENDÊNCIAS:
- Corrigir/validar os casos reais de `?`/`IDENTIFICANDO` em reservas e seletor; validar arte oficial instalada no launcher Samsung.
- Executar build Standard com runner/SDK operacional; conferir Manifest, recursos, assinatura e SHA-256 antes de entregar APK 0.5.26.

PRÓXIMO PASSO:
- Diagnosticar o pipeline de build com runner efetivamente iniciado; testar reservas/trocas e ícone no APK gerado.

### 2026-10-07 — Compatibilidade de ícones do launcher e nova tentativa de APK

Versão: 0.5.26  
Branch: `release/0.5.26`  
Commits:
- `5b939efb4bddf8303952001ca248ba03c45a64eb` — fallback legacy `ic_launcher`;
- `b3605eeab9829f308de98319f1024d742dab562a` — fallback legacy `ic_launcher_round`;
- `7fe575e689c681778ab391bcf1b906bac9b242eb` — invariantes de build para ambos os recursos.

OBJETIVO/PROBLEMA:
- Corrigir a ausência de recursos de launcher para Android pré-API 26 e tentar gerar APK Standard da versão real.

ALTERAÇÕES:
- Confirmada no GitHub a arte oficial `drawable-nodpi/pvppokego_launcher.png` (Poké Ball com setas vermelha/laranja e azul/ciano).
- Confirmado que os dois adaptive icons (`mipmap-anydpi-v26`) apontam para o foreground oficial.
- Criados `app/src/main/res/mipmap-anydpi/ic_launcher.xml` e `ic_launcher_round.xml`, ambos usando a arte oficial nos Androids anteriores ao API 26.
- `.github/workflows/release-0.5.26.yml` agora verifica a existência e referência dos dois fallbacks legacy além dos adaptive icons.
- Nenhum código de batalha ou função existente removido.

ARQUIVOS PRINCIPAIS:
- `app/src/main/res/mipmap-anydpi/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml`
- `.github/workflows/release-0.5.26.yml`

TESTES:
- Inspeção estática da arte original, Manifest, foreground e recursos adaptive/legacy.
- Reexecutado o run `37701971801`: job `113068723701` falhou sem steps.
- Run `37702512658`: job `113069070074` falhou com `steps=null`.
- Run `37702558022`: job `113069219336` falhou com `steps=null`.
- Nenhuma compilação, teste Gradle ou validação em celular ocorreu: o GitHub não atribuiu runner efetivamente executável; ambiente local sem Android SDK/Gradle e sem acesso DNS aos repositórios de dependências.

RESULTADO:
- Recurso ausente de launcher legacy corrigido no código; ainda depende de APK real para validar em dispositivo.
- Não foi criado APK 0.5.26 nesta etapa; não há evidência de erro de compilação do novo código.
- A falha pré-execução pode envolver cobrança/limites ou indisponibilidade da execução hospedada; verificar a anotação exibida pelo GitHub Actions e as configurações da conta antes de diagnosticar como causa confirmada.

STATUS:
PARCIAL / BLOQUEADO (build).

PENDÊNCIAS:
- Desbloquear GitHub-hosted runner (verificação da conta `LucianoToscano`: Settings > Billing & licensing > Actions/budgets/limites e anotação do workflow).
- Rodar `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, validar APK, ícone adaptativo e legado, assinatura e SHA-256.
- Validar no Samsung os ícones do launcher e os retratos dos Pokémon de reserva.

PRÓXIMO PASSO:
- Com runner funcional, deixar o workflow Standard concluir e baixar o artefato efetivamente produzido; registrar checksum, teste e link na documentação.


### Adendo ao registro anterior — checklist e espelho sincronizados

- Checklist 0.5.26 atualizado no commit `e7348403c5d4b19f3c5c5191b423662c2955f2d7`.
- Espelho do Guia Mestre no Google Drive (`178P8tWol8mjv_juy5qhyitEvE6yl1fzPnuLWGDV1cXw`) atualizado e conferido via conector nativo.
- Último run `37702558022` não gerou artefatos (0), com job `113069219336` em failure e `steps=null`.
- Nenhum APK renomeado, publicado ou entregue indevidamente.


### 2026-10-07 — Diagnóstico confirmado: faturamento/limites bloqueiam Actions

Versão: 0.5.26  
Branch: `release/0.5.26`  
Status: BLOQUEADO (infraestrutura/conta GitHub; não é falha de compilação demonstrada)

EVIDÊNCIA:
- Usuário enviou captura do workflow `PvPPokeGo 0.5.26`, execução #58, aba Anotações.
- Erro explícito exibido pelo GitHub: "O serviço não foi iniciado porque houve falhas nos pagamentos recentes da sua conta ou porque seu limite de gastos precisa ser aumentado. Verifique a seção 'Faturamento e planos' nas suas configurações."
- O aviso separado de migração futura de `ubuntu-latest` para Ubuntu 26 não é a causa apontada para esta falha.
- Isso explica as falhas de jobs sem steps/log; o runner foi bloqueado antes de executar código. Ainda não se sabe se o motivo específico é pagamento pendente ou orçamento/limite de Actions.

ALTERAÇÕES:
- Somente documentação do diagnóstico. Código e recursos do aplicativo preservados.
- É necessário que o proprietário da conta verifique GitHub > Settings > Billing & licensing / Budgets and alerts e regularize o impedimento; autorização ao assistente não concede capacidade para alterar pagamentos ou limites da conta.

TESTES:
- Anotação analisada visualmente; nenhum novo Gradle/assemble executado.

RESULTADO:
- Bloqueio de conta/limite identificado na anotação do GitHub; não adianta repetir build até haver liberação do runner.
- APK Standard 0.5.26 permanece NÃO GERADO; nenhum arquivo anterior deve ser renomeado como novo.

PRÓXIMO PASSO:
- Após o GitHub indicar faturamento/budget normalizado, reexecutar o workflow de build Standard, verificar execução real de steps, testes, manifest, ícones, assinatura e SHA-256, e só então entregar o APK.


### 2026-10-07 — Espelho público isolado de compilação Standard

Versão: 0.5.26  
Branch privada canônica: `release/0.5.26`  
Repositório de build público: https://github.com/LucianoToscano/PvPPokeGo-APK-Build  
Snapshot original: `ec8610ba16ea8528e2f9c0268170d5a869d0387c`

OBJETIVO:
- Contornar bloqueio do GitHub Actions em repositório privado sem contratar serviço pago, preservando histórico e acesso privado ao projeto original.

ALTERAÇÕES:
- Foi confirmado repositório público de build separado `LucianoToscano/PvPPokeGo-APK-Build`, com permissões de edição.
- Criado snapshot novo sem histórico privado, contendo 104 arquivos pertinentes ao app Standard, testes, workflow, assets essenciais e avisos de licença.
- Excluídos scripts antigos, `.build/`, documentos internos, Recorder Compat, histórico e chaves de assinatura/credenciais.
- Arte oficial PNG copiada com SHA Git idêntico ao original; lógica de reservas preservada.
- Commit inicial público de código: `21ed3da78944407563e546280715ea950394261c`; trigger README: `9d265d8c807c89a56e27936cfe0510ffae4a6eb1`.
- Workflow público `.github/workflows/build-standard-apk.yml`: empacotar PvPoke offline e atlas, executar testes, gerar Standard debug, validar integridade e SHA-256, publicar artifact QA.
- Primeira tentativa pública `37706678401` executou runner (faturamento não bloqueou) mas falhou na ação `android-actions/setup-android@v3`, que tentou instalar pacote SDK obsoleto `tools`.
- Corrigido workflow para usar `sdkmanager` do runner; commit `1139a2fc256dddcaa05e0d3695db432a34725349`, nova tentativa `37706765046`.

STATUS:
PARCIAL — repositório público e workflow criados, build novo em andamento. Não afirmar APK concluído antes de teste e artifact real.

PENDÊNCIAS:
- Verificar desfecho da execução pública, corrigir eventuais falhas reais de compilação e confirmar artifact APK Standard e assinatura debug.
- A cópia pública é snapshot manual, não sincroniza alterações automaticamente com branch privada.
- O código dos créditos do aplicativo preserva contato previamente definido pelo projeto; código exposto na cópia pública.
- Registrar SHA-256 e link de entrega quando efetivamente disponíveis.

PRÓXIMO PASSO:
- Concluir build público e realizar validação de artefato sem modificar a branch privada de código.


### 2026-10-07 — Erros Kotlin de compilação Standard revelados pelo runner público

Versão: 0.5.26  
Branch privada: `release/0.5.26`
Compilação pública: `LucianoToscano/PvPPokeGo-APK-Build`

PROBLEMA:
- Primeiro build completo com Gradle na cópia pública (run `37706823681`) passou pelo SDK, dependências PvPoke e atlas de sprites; falhou em `:app:compileStandardDebugKotlin`.
- Logs mostram 3 causas concretas: retorno Boolean onde se esperava `HpFill?`, expressão de pontuação misturando tipos numéricos e falta de import `CaptureHealth` no serviço do overlay.

CORREÇÕES APLICADAS NO CÓDIGO CANÔNICO:
1. `BattleFrameEventDetector.kt`: ROI inválida do HP das reservas retorna `null`, não `false`; commit `9e395f5c8492bfac0ea4ce79e1eaa45ab174966b`.
2. `BattleEngine.kt`: `hpAdjustment: Double` calculado com conversão explícita para preservar aritmética do reserveScore sem ambiguidades; commit `c9295962d7d2caac67869dffc36b123ccaa442f5`.
3. `BattleOverlayService.kt`: import de `com.lucianotoscano.pvppokego.data.CaptureHealth` para chamadas já existentes; commit `2a98de5621de5f80e4ed1bc22a4c7db3f9f92b89`.

BUILD PÚBLICO:
- Mesmos três arquivos copiados do código canônico, em um único commit público `0b1c453add6e838c4de06eb07e5792481bb884d2`.
- Nova execução pública: `37707859527`.

TESTES:
- Erros fundamentados no log real da execução `37706823681`. Não houve ainda novo resultado de compilação confirmando as três correções.
- Sem regressão intencional: funções e lógica de batalha preservadas.

RESULTADO:
- Código-fonte atualizado nas duas branches e build público disparado.
- Não existe ainda APK 0.5.26 validado.

STATUS:
PARCIAL — aguardando resultado do teste e compile Android da nova execução.

PRÓXIMO PASSO:
- Revisar `37707859527`; em sucesso verificar integrity, assinatura, SHA-256 e disponibilizar artifact, registrar link/checksum. Se falhar, corrigir apenas o erro real indicado pelo novo log.


### 2026-10-07 — APK Standard 0.5.26 QA/debug REAL entregue

Versão: `0.5.26` (versionCode 31)  
Branch privada canônica: `release/0.5.26`  
Build público isolado: https://github.com/LucianoToscano/PvPPokeGo-APK-Build
Commit compilado: `0b1c453add6e838c4de06eb07e5792481bb884d2`
Run: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37707859527

OBJETIVO:
- Gerar pela primeira vez o APK 0.5.26 real com os recursos de launcher/reserva e correções Kotlin, preservando repositório original privado e Recorder Compat adiado.

ALTERAÇÕES E RESULTADOS:
- SDK Android 36, Gradle, PvPoke empacotado offline e banco visual de fingerprints/atlas executados com sucesso no runner público.
- Os três erros Kotlin revelados na execução anterior foram corrigidos primeiro no original privado (`9e395f5c`, `c9295962`, `2a98de56`) e depois copiados sem divergências para a branch pública (`0b1c453`).
- Workflow `37707859527` terminou `success`: `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, validação de integridade e assinatura.
- Artefato produzido: `PvPPokeGo-0.5.26-QA-debug.apk`, 78.046.047 bytes.
- Assinatura APK v2 debug verificada. **Não é certificado permanente/release**, e pode não atualizar um APK antigo assinado de outra forma.
- ZIP GitHub artifact extraído em ambiente de entrega, `unzip -t` do APK passou; foram confirmados `assets/gamemaster.json`, `assets/pokemon_icon_atlas.png`, `assets/pvpoke_snapshot.json`, `res/drawable-nodpi-v4/pvppokego_launcher.png`.
- SHA-256 publicado pelo workflow e reconfirmado após download: `461815c2b8c9078f8fcd617145f1abeefb3a0be03f2376114227f44594708932` (match exato).
- Upload para Google Drive concluído e verificado via metadados (tamanho idêntico):
  https://drive.google.com/file/d/1bcbP8WJ7sWgd9lUTvqb0NzV9MSXq_nCH/view?usp=drivesdk
  Pasta: `12g6IITrn64y0D40YsIdzl-sN9alZty8Q`.
- Checklist atualizado com entrega QA no commit `f3d5ab0c7d86f9d8d9a5e77f20d2de67603dd3af`.

ARQUIVOS PRINCIPAIS:
- `BattleFrameEventDetector.kt`, `BattleEngine.kt`, `BattleOverlayService.kt` (correções Kotlin)
- `docs/CHECKLIST-PvPPokeGo-0.5.26.md`
- Na cópia pública: `.github/workflows/build-standard-apk.yml`, 104 arquivos de snapshot de build (sem histórico privado, segredos e docs internas)

TESTES:
- Compilação Android Standard e testes unitários de Gradle concluídos no CI (workflow SUCCESS).
- Validação de assinatura, integridade ZIP e checksums: PASS.
- Sem QA visual em aparelho nesta etapa.

STATUS:
CONCLUÍDO — geração e entrega QA/debug do APK Standard 0.5.26.  
PARCIAL — validação física dos ícones de reservas e launcher Samsung e assinatura permanente.

PENDÊNCIAS:
- Usuário instalar e verificar reservas 2/3, troca, ícone Samsung, telas com CP duplicado, e informar eventuais regressões.
- Se o certificado instalado diferir, exportar ajustes antes de desinstalar versão anterior.
- Release permanente só com keystore conhecida; não renomear QA/debug como release final.
- Cópia pública build-only é snapshot e requer sincronização explícita das alterações futuras.

PRÓXIMO PASSO:
- Reproduzir visualmente os bugs em dispositivo usando o APK entregue; aplicar correção na branch privada canônica, transferir snapshot revisado ao build público, registrar novo checkpoint e subir numeração para próxima entrega.


### 2026-10-07 — 0.5.27: reversão do visual poluído das reservas e ícone oficial transparente

BASE: 0.5.26 validado no Actions `37707859527`.  
BRANCH CANÔNICA: `release/0.5.27`, criada de `release/0.5.26` no commit `0faf11d2227c4a4ddc17990d6fd476832526d2b8`.  
COMMIT DA CORREÇÃO: `cb00b81607706df99fcd9837dd27fc6e4fc4c4bb`.  
ESPÊLHO PÚBLICO: `LucianoToscano/PvPPokeGo-APK-Build`, commit `71a849787cfad9aead891cabd6e48b7772dce4e8`.

PROBLEMAS REPORTADOS PELO USUÁRIO NA CAPTURA DA VERSÃO 0.5.26:
- Cards das duas reservas na direita visualmente poluídos, retratos com halos/círculos, textos e CP muito comprimidos.
- Elementos de HUD pareciam desaparecer, especialmente durante troca em que a identidade dos dois cards nem sempre está confirmada.
- Launcher instalado diferente da arte transparente enviada explicitamente.

ALTERAÇÕES:
- `BattleOverlayView.kt`: cards com fundo escuro arredondado compacto, indicador menor, retrato quadrado arredondado sem halo circular, nome/CP/status em linhas separadas, texto limitado à largura disponível, tipos menores e HP barra discreta, mantendo posições personalizáveis individuais das duas reservas.
- Em switch chooser só são desenhados indicadores especiais quando ambos os cards têm correspondência confirmada; nos demais casos o HUD de reservas continua visível sem esconder os elementos estabelecidos.
- Prioridade de sprite por speciesId/form do atlas, depois retrato de slot estável e só então crop card confirmado; a correção de correspondência não foi removida.
- `app/src/main/res/drawable-nodpi/pvppokego_launcher.png`: substituído pelos bytes EXATOS do PNG RGBA transparente enviado pelo usuário (1254×1254, 1.453.830 bytes, Git blob `95797dfe8b1fe4e4db0fe07d1f35c2a4e49ae695`).
- `colors.xml`: fundo do adaptive icon transparente `#00000000`; foreground mantém a logo original com inset de 16dp para evitar corte pela máscara; `mipmap-anydpi` e `mipmap-anydpi-v26` existentes permanecem referindo o mesmo recurso sem criar ícone alternativo.
- `app/build.gradle.kts`: versionCode 32, versionName 0.5.27.
- Repositório privado permanece privado; cópia pública isolada atualizada e configurada para build 0.5.27.

TESTES/BUILD:
- GitHub Actions do espelho público: `37712804512` disparado; status ainda por confirmar neste registro.
- Não foi executado teste visual em Samsung nesta etapa; layout novo requer QA físico.

STATUS: IMPLEMENTADO NO CÓDIGO; APK/QA EM ANDAMENTO.
PENDÊNCIAS: confirmar `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, artefato APK 0.5.27 com hash/assinatura, e testar launcher/reservas/troca no Samsung. Não declarar reparo visual validado sem prova.
PRÓXIMO PASSO: conferir o resultado do run público, ajustar qualquer erro de compilação, entregar APK QA se passar e registrar checksum/URL no Guia Mestre.


### 2026-10-07 — Entrega QA 0.5.27 concluída e hash da arte conferido

BRANCH CANÔNICA: `release/0.5.27`  
REPO ORIGINAL: privado; espelho público apenas para build, sem histórico privado.  
COMMIT FONTE COM CORREÇÕES: `cb00b81607706df99fcd9837dd27fc6e4fc4c4bb`.  
COMMIT DE BUILD: `71a849787cfad9aead891cabd6e48b7772dce4e8`.  
EXECUÇÃO SUCCESS: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37712804512

RESULTADO:
- `PvPPokeGo-0.5.27-QA-debug.apk` gerado, 79.193.451 bytes (versionCode 32, versionName 0.5.27).
- Testes e build `:app:testStandardDebugUnitTest` + `:app:assembleStandardDebug`: **PASS**.
- ZIP APK verificado sem erros, assinatura APK Signature Scheme v2 **PASS** (certificado QA/debug, não release permanente).
- SHA-256 do APK: `cbf336cddfa31bf0639df41491b4e1cc0019b3bc4403a6c394a3bfa5966d3025` (arquivo baixado bate exatamente com SHA256SUMS do CI).
- `res/drawable-nodpi-v4/pvppokego_launcher.png` extraído do APK tem 1.453.830 bytes e SHA-256 `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`, **exatamente igual ao PNG RGBA transparente enviado pelo usuário**.
- `pokemon_icon_atlas.png` e `gamemaster.json` presentes dentro do APK.
- Arquivo ZIP de distribuição contendo APK + SHA256SUMS e verificação de assinatura foi armazenado e conferido na pasta oficial do Google Drive:
  https://drive.google.com/file/d/1KIvivodofc3QA72oS0zeUTJPplFUkUb3/view?usp=drivesdk
- Checklist 0.5.27 atualizado: commit `c12bdec830797e21009ebaa0951d15b7d285d040`.

STATUS:
- CONCLUÍDO: correção no código, build e validação técnica do APK QA 0.5.27.
- PENDENTE: teste visual no Samsung; não afirmar que aparência do launcher ou cards foi aprovada pelo usuário.
- IMPORTANTE: assinatura debug nova pode impedir instalação por cima da 0.5.26 devido a certificado diferente. Exportar configurações antes de desinstalar, se necessário.

PRÓXIMO PASSO:
- Instalar, validar o PNG oficial na tela inicial, reservas (PC, tipos, retratos e HP), modo de troca/continuidade e ícones de controle no Samsung. Registrar feedback e evoluir para próximo versionCode em nova entrega.


### 2026-10-07 — 0.5.28: restauração visual baseada no APK 0.5.17

RELATO:
- Usuário disse que 0.5.27 estava "tudo bugado" e enviou o APK **PvPPokeGo-0.5.17.apk** como referência visual aprovada.
- O arquivo fornecido foi verificado como ZIP/APK íntegro (9 dex, 27 assets, SHA-256 `928fa6cfb64be14842721ce248a055f7aee1a7f9c40d7d3eaeb7b1045ddfbbd6`). Não foi executado em emulador/celular; a análise de HUD usa também a branch fonte preservada `release/0.5.17` (HEAD `d5c3be748b54eb10cd73bb116d6eda1e8acdbf85`).
- Comparação com 0.5.27 provou mudança de apresentação: a 0.5.17 tinha indicadores pequenos de matchup, tipos e recomendação, sem cards retangulares grandes, retratos ou barra de HP no bloco de reservas. A 0.5.27 inseriu esses cards e textos, alterando o layout original.
- Outro desvio: a seção de ataques 0.5.27 mostrava previsão extensa e indicadores de dano mesmo no HUD normal; 0.5.17 mostrava círculos dos moves, nomes, eficácia e texto simples de energia.

DECISÃO:
- A **0.5.17 é referência canônica para a aparência do HUD**, não para a lógica de batalhas.
- Preservar toda a implementação avançada de engine/capture/reconhecimento e ícone do app transparente fornecido pelo usuário em 0.5.27; NÃO fazer rollback geral.

ALTERAÇÕES REALIZADAS:
- Criada branch privada `release/0.5.28` baseada em `release/0.5.27` (`09d12f524652cd8fd106fda029a24f8c4bee2ced`).
- `BattleOverlayView.kt` em `33878a17a904953c2ce91aa1e50390bccbb31d5b`: restauradas as rotinas `drawReserves` e `drawSwitchChoiceIndicators` da branch 0.5.17; removidos os cards grandes, retratos decorativos e texto extra do HUD normal. Única salvaguarda adicional: não ocultar automaticamente o indicador quando OCR não encontra nome; manter "IDENTIFICANDO". Indicador de switch mantém ícones pequenos da 0.5.17 e fallback quando dados do chooser estão incompletos.
- `BattleOverlayView.kt` em `c03ff271a9bf727f9df8973ac775d738c7d8c27d`: restaurado desenho tradicional de `drawMoveTriangle` (nome dos 3 ataques, counter, energia resumida, espaçamento original); dados e funções preditivas continuam na engine e as anotações avançadas ainda podem ser vistas com `settings.debugMode`.
- `app/build.gradle.kts` em `0ad039f11b7deb39c35ef564dcbcab15c0df9900`: `versionName = 0.5.28`, `versionCode = 33`.
- Ícone do launcher **não revertido** ao arquivo antigo: mantido exatamente o PNG oficial transparente de 0.5.27 solicitado pelo usuário.
- A cópia pública isolada para compilação foi atualizada no commit `c1432f2b316c06baf40083db5d3f40f94a03bef5`, sem exposição do histórico privado.
- Workflow: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37715432893

ARQUIVOS:
- `app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt`
- `app/build.gradle.kts`
- Espelho de build público: `.github/workflows/build-standard-apk.yml`, `README.md`.

VALIDAÇÃO:
- Integridade ZIP do APK enviado 0.5.17: PASS.
- Revisão estática do código fonte 0.5.17 vs 0.5.27: o traçado de olho/cadeado, indicadores, chips de tipo e posições padrão já eram idênticos; não houve razão para substituir esses controles.
- A versão Standard 0.5.28 aguardava build/tests ao escrever este registro. NÃO marcar APK disponível sem artefato e assinatura reais.

STATUS:
- Código 0.5.28 com visual legacy restaurado, build em andamento.
- QA real de tela em Samsung ainda obrigatório; nenhuma garantia de que efeitos de layouts personalizados antigos/PGSharp sejam resolvidos apenas pelo desenho.

PENDÊNCIAS:
- Verificar `:app:testStandardDebugUnitTest` + `:app:assembleStandardDebug`; conferir assinatura v2, hash e artefato.
- Testar em dispositivo: olho, cadeado, ataque inimigo, indicador de reservas, opção de troca, posições salvas e ícone transparente do launcher.
- Se o usuário ainda reportar diferença, pedir captura da 0.5.17 executando, além do APK, para comparar áreas e dimensões reais.

PRÓXIMO PASSO:
- Finalizar QA/debug 0.5.28, entregar APK verdadeiro e registrar entrega no Guia Mestre/checklist.

### 2026-10-07 — Correção de compilação no HUD restaurado

- Run público `37715432893`: falhou na compilação Kotlin; log apontou `BattleOverlayView.kt:873:22 Unresolved reference 'stableBattleAdvice'`.
- Causa: no recorte de código visual da 0.5.17, o helper novo `stableBattleAdvice` da 0.5.27 foi removido junto com o bloco da UI moderna, mas `drawBattleAssist` ainda depende dele.
- Corrigido no privado `f1de2771f5a0e88a840d8864134ea5424cdd8d5a`: recuperado exatamente o helper, mantendo a estabilização dos conselhos.
- Espelhado no repo público `491106a928efd810c9351c8e356d3513ad03c6f2`; novo run `37716045867` iniciado.
- Comparados os métodos removidos da versão 0.5.27: `drawReservePokemonIcon`, `reserveMatchupLabel`, `reserveMatchupColor` e `fitReserveText` não têm chamadas pendentes após a restauração.
- Status: compilação e entrega APK 0.5.28 ainda pendentes neste checkpoint.


### 2026-10-07 — APK Standard QA 0.5.28 gerado e verificado

CONTINUIDADE DO RELATO "VEJA AQUI COMO ERA": usamos a fonte `release/0.5.17` e o APK fornecido para restaurar visual sem rollback de engine.

RESULTADO REAL:
- Novo APK `PvPPokeGo-0.5.28-QA-debug.apk`, 79.193.451 bytes, com versionCode 33/versionName 0.5.28.
- Run final: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37716045867 — **SUCCESS**.
- Commit público compilado: `491106a928efd810c9351c8e356d3513ad03c6f2`.
- Testes Standard unitários `:app:testStandardDebugUnitTest`, assemble APK `:app:assembleStandardDebug`, verificação do launcher e dados PvPoke/atlas: PASS.
- `unzip -t` dentro do APK: PASS; `assets/gamemaster.json`, `assets/pvpoke_snapshot.json`, `assets/pokemon_icon_atlas.png`, `assets/pokemon_icon_atlas.json` e `res/drawable-nodpi-v4/pvppokego_launcher.png` presentes.
- Assinatura `apksigner verify`: v2 **true**, 1 signatário QA/debug, não assinatura permanente de release.
- SHA-256 APK: `504643fe67f08ab599f35d74b8cf3cf1f7f079324badae9024712d59c54c359d`, idêntico ao manifest de checksums do runner.
- PNG oficial transparente no APK: SHA-256 `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`, idêntico ao fornecido pelo usuário.
- Artefato público: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37716045867
- ZIP de distribuição e verificação, armazenado na pasta oficial Drive: https://drive.google.com/file/d/1sPMZZMsKpWukN0J3yxhBYVGYeJHu4RYe/view?usp=drivesdk
- Checklist final 0.5.28 atualizado no commit `d24c905f7487035f4658a33ad0a3402797ce7cd3`.

COMPROMISSO DE QUALIDADE:
- Foram recuperados traçados HUD 0.5.17 de reservas e escolhas de troca (pequenos badges e ícones de tipo, sem grandes cards) e disposição clássica de ataques/energia. Debug mantém novas previsões. Nem a engine nem o ícone transparente oficial foram revertidos.
- **Não houve teste visual no Samsung**. A restauração está validada como código/build, NÃO como correspondência perfeita no dispositivo.
- APK 0.5.28 QA/debug pode não atualizar 0.5.27 devido a certificado de debug; recomendar exportar dados/posições antes de desinstalação.

PENDÊNCIAS:
- Usuário instalar e validar a aparência da 0.5.28 lado a lado com a 0.5.17, incluindo reservas, trocas, tipos, cadeado, olho, ataque inimigo e posição dos controles.
- Ajustar com evidência visual qualquer diferença residual, sem nova reformulação desnecessária.

### Continuidade após relato de falha de reconhecimento dos aliados — versão 0.5.29

Após o APK 0.5.28, o usuário confirmou que o sistema de identificação dos aliados já não reconhecia seus Pokémon como na 0.5.17. A nova branch canônica é [`release/0.5.29`](https://github.com/LucianoToscano/PvPPokeGo/tree/release/0.5.29). Consultar o [Guia Mestre 0.5.29](https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md) e o [Checklist 0.5.29](https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/CHECKLIST-PvPPokeGo-0.5.29.md) antes de mudanças. A branch 0.5.28 permanece preservada como referência de layout, não como versão ativa para reconhecimento.

## 12. Regra final

Sempre que o usuário pedir:
- “me manda o texto para novo chat”;
- “continue em outro chat”;
- “não quero perder o contexto”;

a resposta deve incluir obrigatoriamente o link para este arquivo e instruir o novo chat a lê-lo primeiro.

**Este documento deve crescer junto com o projeto. Não é descartável e não deve ser substituído silenciosamente por um resumo novo.**
