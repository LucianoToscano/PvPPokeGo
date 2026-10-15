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


### 2026-10-07 — Correção funcional do reconhecimento dos três aliados (0.5.29)

CONTEXTO:
- Após entregar 0.5.28 com HUD visual restaurado de 0.5.17, usuário informou que o SISTEMA não funcionava; detalhou que o reconhecimento dos Pokémon aliados piorou comparado com 0.5.17.
- Branch base privada `release/0.5.28` preservada; nova branch `release/0.5.29` criada, versionCode 34.
- A análise confirmou uma regressão concreta: `BattleOcrDetector.fuseTeamSlotEvidence` da 0.5.28 retornava null para nome e PC válidos quando não havia match de imagem, ao contrário do detector 0.5.17 que reconhecia aliados por OCR.
- A 0.5.17 varria uma área ampla 4–96% largura / 10–93% altura. A nova implementação só usava três regiões fixas estreitas, potencialmente perdendo outras proporções de layout.
- O motor interrompia buscas se havia três slots com PC, ainda que identidade fosse incerta.

MUDANÇAS:
- `BattleOcrDetector.kt` commit `d37d3fe484bda3a8d120d47ed9763530f85a3c97`: OCR puro nome válido + PC 10..10000 e confiança ≥0.90 recuperado independentemente da existência de sprite. Não substitui identificação visual para formas/variantes quando disponível. Se per-slot identifica menos que os 3, usa leitura ampla da tela de seleção 0.5.17. Este fallback só retorna um time completo de 3 pares distintos de espécie/PC reconhecidos, evitando introduzir falsas identidades de tela sem time.
- `AllyOcrRecoveryPolicy.kt` commit `17f68e42a663e6fcf8b8e895ff789749ef0ca11f`: regra pura para reconhecimento OCR.
- `AllyOcrRecoveryPolicyTest.kt` commit `062cbb3b62059bf48eaf8822582aa7d025d1534d`: regressões de nome+PC com visual ausente e rejeição de PC/nome inválidos e baixa confiança.
- `BattleEngine.kt` commit `73029729981e308bac1c6990e91ca796da997e5d`: `needsOwnTeamScan` só interrompe depois que os três slots têm PC, espécie e confiança ≥0.90, evitando que palpites incompletos congelem o time.
- `BattleOverlayService.kt` commit `ba1668ac8244911fec9c22272b84eadb0e0b99be`: cadência OCR da equipe 1550ms, próxima ao detector amplo da 0.5.17, para reduzir carga no aparelho.
- `app/build.gradle.kts` commit `1e45dec7dfc80eebc1a6eddffda3df78b7a0bdc2`: versionName 0.5.29, versionCode 34.
- Copiado só o snapshot necessário para `LucianoToscano/PvPPokeGo-APK-Build` público, commit `008993b11f9299fc4a88cfeb7fde9f8ff7e9efc2`, preservando original privado e visual de 0.5.17 restaurado na 0.5.28.

VALIDAÇÃO:
- Build Standard CI iniciado: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37719025048
- STATUS NESTE CHECKPOINT: teste compilação em andamento. Ainda não declarar reconhecimento validado no Samsung.
- Sem captura bruta de tela / arquivo de diagnóstico do usuário para testar a exatidão da identificação de espécies e PC no dispositivo real.
- A regra OCR+PC recupera comportamento antigo, mas a leitura ampla é deliberadamente conservadora e não resolve automaticamente todos os layouts.

PENDÊNCIAS:
- Confirmar testes unitários, compile e APK 0.5.29. Verificar assinatura v2/sha256/ícone ao entregar.
- QA físico na tela de seleção 3 aliados e continuidade das reservas durante trocas; solicitar exportação de diagnóstico se continuar falhando.
- Não alterar ícone transparente e layout de referência 0.5.17 nem perder motor de batalha.


### 2026-10-07 — APK 0.5.29 com recuperação de aliados: entrega técnica concluída

COMMIT DO ESPELHO DE BUILD: `008993b11f9299fc4a88cfeb7fde9f8ff7e9efc2`  
RUN: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/37719025048

RESULTADO:
- Run público concluiu com **success** em `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug` e validação do APK.
- APK de QA: `PvPPokeGo-0.5.29-QA-debug.apk`, 79.193.455 bytes, versão 0.5.29, versionCode 34.
- SHA-256 do APK validado localmente contra `SHA256SUMS.txt` do build: `b79c41f58e6fe846f7a62497cd91e1d64f15e377bdee1d8dcc4e2369e1199e3a`.
- `unzip -t` verificou arquivo sem erros; atlas, gamemaster e snapshot PvPoke encontrados no APK.
- Assinatura v2 debug verificou **true** (1 signer); não confundir com certificado de release/permanente.
- Logo PNG transparente oficial no APK: SHA256 `f8d4f45922e6af0022e3eaad1d255fedd2b790650f542e62315042f8bd1dbf51`.
- Pacote ZIP armazenado no Google Drive oficial, com APK, assinatura e hashes:
  https://drive.google.com/file/d/1Pw9RuiruLHrP4f7tkCFDLG8NnGhMuxFO/view?usp=drivesdk
- Checklist atualizado no commit `3862b9b4ca9285f0e4e7f1c3bfd08076483fd9b4`.

STATUS:
- CONCLUÍDO: correção-fonte do fallback OCR nome/PC + região ampla da versão 0.5.17, testes automatizados, APK Standard QA 0.5.29 gerado/assinado e distribuído.
- PENDENTE: confirmação de reconhecimento correto dos 3 aliados na seleção pré-batalha no Samsung e preservação após trocas. Nenhum teste em aparelho foi executado pelo assistente.
- Se continuar falhando, obter screenshot e `Configurações > Exportar diagnóstico` para analisar layout/captura/OCR e não voltar a modificar apenas a aparência.
- Se a assinatura instalada divergir, exportar configurações antes de desinstalar para testar o APK QA/debug.

PRÓXIMO PASSO: QA de reconhecimento no aparelho; investigar código de captura e pipeline usando diagnóstico caso espécies/PC não sejam detectados.


### 2026-10-08 — Adendo canônico: inventário visual, OCR, energia, HP e previsões da batalha

**Natureza:** decisão de produto + especificação funcional e visual derivada das capturas e correções feitas pelo usuário nesta conversa. **Registro documental, NÃO alegação de implementação no APK**. Base visual historicamente aprovada: HUD compacto no estilo 0.5.17/0.5.28, preservando o motor evoluído; não fazer rollback de código. Regra permanente: **MELHORAR E ORGANIZAR, NUNCA EMPOBRECER**.

**Referências e branch/HEAD verificados em 2026-10-08, antes deste adendo:**
- Repositório privado: https://github.com/LucianoToscano/PvPPokeGo ; branch padrão main, não confundir com branch ativa.
- Guia solicitado: release/0.5.26, HEAD observado 88d675f516e370dd0f684bd16c4f2c9c6fb5210d; essa branch é histórica, preservada.
- release/0.5.27, HEAD observado d88ccb84e7bc582aed49bb99a7c223fc903567f1; histórica.
- release/0.5.28, HEAD observado c184c3b39318eb00d6ede6bed6b32fd998291d80; referência visual mais recente, histórica.
- **Branch ativa real release/0.5.29**, HEAD observado 60f5a87ed71886831e676d7b1b1f0745a67d3754. Guia ativo: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md . Não voltar para 0.5.26 apenas porque o link foi citado.
- Objetivo desta edição: acrescentar registro integral tanto ao guia histórico explicitamente apontado quanto ao guia ativo; código, versão, workflow, release e APK não alterados. Os HEAD acima são **pré-edição**, não se confundem com os commits documentais novos.

#### A. Limites de propriedade e integridade da interface — REGRA INVIOLÁVEL

1. **Interface original do Pokémon GO é intocável**. Jamais modificar, reposicionar, redimensionar, substituir, tapar ou interferir nos controles originais. O PvPPokeGo pode observá-los por OCR/visão, construir estado e desenhar um overlay independente que não bloqueie toques necessários.
2. Fontes NATIVAS, **não contam como ícones/funções próprios do PvPPokeGo**: cartões superiores de cada Pokémon com nome, PC, Pokébolas e escudos; barras originais de HP; personagens, cena e animações; botão nativo de desistir/trocar; cartões nativos dos dois aliados reservas com sprites/PC/HP; grandes botões dos ataques carregados com seu preenchimento e nomes; textos nativos de eficácia (SUPER EFICAZ!, NÃO É MUITO EFICAZ); mensagens de ataque (ex.: “Sneasel usou Jogo Sujo!”), BLOQUEADO!, ATAQUE/DEFESA AUMENTOU ou DIMINUIU; tela de escolha de escudo, botão AGORA NÃO, contador de decisão; modal de substituição obrigatória e contador; telas pré-batalha, escolha de liga/equipe e seus emblemas. **São entradas de leitura, não componentes a redesenhar**.
3. **PGSharp não é PvPPokeGo.** Excluir do inventário próprio: estrela amarela e barra cinza/botões “Ataques ágeis”, “Ataques carregados” e “Shield”, assim como outros controles atribuídos ao PGSharp pelo usuário. Não transformar supostos botões externos em funcionalidades implementadas.
4. Distinguir explicitamente o texto nativo de eficácia perto do HP/botões próprios do Pokémon GO do **indicador adicional de eficácia do inimigo desenhado junto dos círculos do PvPPokeGo** (item #15 das marcações). Nunca documentar esses dois como o mesmo recurso.
5. Ícones circulares de tipos com aparência no estilo Pokémon GO, cor por tipo, contorno fino, escala pequena e alinhamento horizontal **já foram aprovados** pelo usuário; **preservar exatamente o conceito e evitar redesenho gratuito**.

#### B. Inventário visual consolidado do PvPPokeGo (numeração das capturas e correções do usuário)

A numeração corresponde às anotações manuais das capturas, não necessariamente a IDs de código. Ícones sobre cards nativos são overlays informativos; cards/HP/PC nativos NÃO são propriedade nossa.

| # | Elemento próprio ou leitura | Função / estados / observações |
|---|---|---|
| 1 | Olho riscado / olho aberto | Alternar visibilidade da HUD; com HUD oculta permanece controle mínimo para reexibir. Dois estados visuais observados. |
| 2 | Cadeado fechado / aberto verde | Travar posições / destravar para editar e arrastar HUD. Não interceptar controles do jogo. |
| 3 | Chave inglesa / engrenagem | Acesso ao menu rápido/visual/sistema do overlay; controle visível dentro e fora da batalha. |
| 4 | Badge acima do emblema: ✓ verde, círculo cinza, × vermelho | Matchup do Pokémon ATIVO: favorável, neutro ou desfavorável (tipos, golpes e contexto; não só PC). **Não é botão de fechar**. |
| 5 | Emblema triangular do PvPPokeGo | Identificação visual do overlay e referência para status de matchup; não presumir ação ao toque sem confirmar no código. |
| 6 | Caixa de decisão tática | Estados registrados: CONTINUE FAST, ATENÇÃO, ESCUDO, TROQUE → Pokémon, PREPARE-SE, ANALISANDO ATAQUE. Explicações resumidas: pressão, golpe possível, proteção, alternativa de troca, ataques/tempo até carregado, dano sem/com escudo, faixa de HP/KO; estimativa, não certeza. |
| 7 | Triângulo de três círculos dos golpes do INIMIGO | Superior = ágil identificado/provável; dois inferiores = carregados prováveis; nomes, tipo, setas de seleção, eficácia, contador/pontos, preenchimento energético compartilhado e texto ENERGIA ... ESTIMADA. |
| 8 | Chips pequenos de tipos no topo | Exibem tipos próprios, tipos adversários e/ou tipos ofensivos relevantes/supereficazes conforme posição; manter os símbolos/cores aprovados e rotular a semântica sem confundir tipo real com fraqueza calculada. |
| 9 | Badge na reserva aliada 1 | MELHOR ✓, NEUTRO ○ ou EVITAR × para matchup de troca com Pokémon inimigo ativo; é overlay separado do card nativo. |
| 10 | Badge na reserva aliada 2 | Mesma análise independente do slot 1, com atualização em mudanças de oponente/estado. |
| 11 | Identificação do primeiro Pokémon reserva | O card com sprite, PC e barra HP é **nativo Pokémon GO e só pode ser lido**; o PvPPokeGo adiciona apenas avaliação/ícones próprios adjacentes. |
| 12 | Pequenos indicadores de tipo da segunda reserva | Overlay de tipos/matchup associado ao segundo slot sem alterar o card nativo. |
| 13 | Pequenos valores numéricos laterais (ex.: 8, 25, 32, 46) | **Hipótese a verificar**, possivelmente temporizador de troca/reentrada após switch e memória de inimigo vivo. Não classificar como nível ou contador de turnos sem auditar código e capturas sequenciais. |
| 14 | Telemetria compacta perto da arena | Textos por espécie/golpe e abreviações F/E; possível contagem de Fast e energia estimada. Decodificar unidades no código antes de afirmar sua precisão. Considerar modo diagnóstico recolhível. |
| 15 | SUPER EFICAZ! **próprio sob os carregados do inimigo** | Classificação de eficácia do golpe adversário contra Pokémon ativo; diferente do texto do Pokémon GO. Pode alimentar ameaça, dano e ESCUDO. Ex.: Dive (Água) contra Houndoom (Sombrio/Fogo) é supereficaz. |
| 16 | Setas ▼ ao lado dos nomes dos golpes inimigos | Abrem seletor manual AUTO · PvPoke/OCR: lista #1...#5 de possíveis carregados, tipo, dano base, custo E e ✓ do selecionado. Capturas: Vaporeon Surfar/Pancada Corporal/Raio Aurora/Pulso d’Água/Retorno; Sealeo Avalanche/Jogo Sujo/Soco de Gelo/Pinote Triplo/Retorno. Seleção manual substitui hipótese de move, sem zerar automaticamente energia armazenada. |
| 17 | Pequeno monitor HP 100% acima do centro | Indicação de percentual HP própria do overlay; **qual lado ele representa e acurácia dependem de auditoria**, não assumir que seja valor exato. |
| 18 | PREVIEW textual/numérico de dano à esquerda | Ex.: SEM ESC, dano em faixa percentual, HP sobrevivente, COM ESC, KO POSSÍVEL e MODELO. Previsão deve exibir faixa/confiança, não inventar certeza. |

**Outros elementos de overlay observados, fora da numeração:** cronômetro de sessão 0:00:00 e ícone pessoa/engrenagem associados (finalidade exata pendente); botão de fechar lista de golpes; lista de movimentos no modo AUTO · PvPoke/OCR; textos de tipo/energia/dano; pequenas bolinhas indicando geração/cargas e energia restante; indicação de seleção de golpe; menu de configuração; modo editar HUD; menu de apenas ícones; estados de gravação/visão. NÃO incluir barra PGSharp “Ataques ágeis / Ataques carregados / Shield” nem estrela PGSharp.

**Menu rápido global (capturas fora da batalha também):** Editar posições do HUD; Textos do HUD; Escala (ex. 80%); Opacidade (ex. 90%); Restaurar posições/layout; Mostrar opções avançadas; Gravar tela com PvPPokeGo; Pausar visão/análise; Remover sobreposição; Parar serviço PvPPokeGo; Modo somente ícones; Fechar menu. Em edição, controles inferiores observados: –, +, ◐, L, H, desfazer, ✓ confirmar, restaurar/reciclar; significado exato de ◐/L/H exige leitura do código. Não atribuir a esses controles ações não verificadas.

**Preferência visual:** HUD clean, profissional, minimalista, pequeno, translúcido, ajustável/travável, sem texto redundante, indicadores de tipo originais aprovados, desenhos de círculos compactos, componentes expansíveis só sob demanda, alertas temporários; exibir previsões sem poluir nem obstruir telas nativas de escudo/troca. Visual de reserva 0.5.17/0.5.28 como referência, mantendo todas as capacidades atuais.

#### C. Nova função especificada — três slots inteligentes do time AD-versário (PENDENTE DE IMPLEMENTAÇÃO/QA)

- Exibir **três slots fixos “? ? ?”** inicialmente; à medida que Pokémon adversários forem reconhecidos por nome/OCR/sprite/PC/troca, substituir somente o slot correspondente pelo **sprite correto da espécie/forma**, sem inventar identidade. Manter slots fixos por identidade/revelação; não reordenar a cada troca.
- Slot ativo tem borda/destaque discreto; reserva viva permanece visível com HP/energia armazenados; desmaiado tem tratamento cinza/X, com histórico guardado. Estado desconhecido não recebe nome inferido como fato.
- O usuário aprovou **brilho de energia**: sem brilho = sem carregado pronto confirmado/estimado; brilho suave/cor 1 = energia estimada para **ao menos uma** utilização de carregado; brilho cor 2 = energia estimada para **dois** carregados consecutivos. Pode complementar com • / •• para acessibilidade e evitar ambiguidade. Indicar confiança se move ainda não confirmado.
- **Não confundir dois carregados diferentes equipados com energia para executar duas vezes um carregado.** Cálculo de prontidão baseado na única energia compartilhada, custo de cada golpe e quantidade de utilizações consecutivas realmente possível até limite 100E. Não brilhar se o Pokémon estiver nocauteado, mesmo com energia residual histórica.
- Ícone continua armazenando seus dados quando adversário troca, e retorna ao estado anterior quando ele reaparece; energia e HP **não resetam na troca**.
- Não criar cartões grandes adicionais sobre Pokémon GO; preferir faixa compacta independente sem cobrir componentes críticos.

#### D. Motor de energia e turnos: equações e regras determinísticas

- Um turno de PvP = **0,5 s**; cada ágil tem duração em turnos, dano e energia por utilização próprios. **Não generalizar 10E por ágil nem converter valores de Ginásios/Reides para PvP**.
- Cada Pokémon dos dois lados precisa de registro independente: ID de slot/forma, ativo/reserva/desmaiado, espécie/PC, moves ágeis/carregados e confiança, intervalo de HP, escudos relevantes, energia como intervalo min/provável/max, último evento/turno, buffs/debuffs ativos, histórico.
- Atualização ao reconhecer ágil uma vez: E_nova = min(100, E_anterior + geração_do_golpe); atualizar AMBOS os medidores carregados conforme a **mesma reserva**; primeira cor até custo de uma utilização, segunda cor quando continua guardando para segunda. Se o golpe custar 40E, uma utilização fica disponível em 40E e duas em 80E; se custar 55E, duas não cabem em 100E.
- Ao reconhecer carregado usado: E_nova = max(0, E_anterior − custo_identificado), uma única vez por evento, inclusive se bloqueado. Nunca zerar automaticamente saldo, e nunca descontar em dobro por OCR repetido/câmera/escudo; em evento de golpe desconhecido, preservar intervalo de hipóteses até identificação, não subtrair custo arbitrário.
- Exemplo HIPOTÉTICO de ágil +10E, carregado 40E: 4 usos → 40E, anel cor 1; mais quatro → 80E, segunda cor; gastar 40 → 40E restante. Exemplo REAL a auditar no dataset PvP vigente: Raichu com Trovoada de Choques não deve assumir +10E sem olhar a tabela correta; valores da aba “Ginásios e Reides” não são os do PvP.
- HP caindo na barra nativa é **sinal de dano, não prova isolada de um ágil**: sincronizar duração/animação do golpe, intervalo entre impactos, dano sem outros eventos, fase da batalha e OCR. Dano pode vir de carregado, lag, frames perdidos ou mais de um golpe entre leituras. Priorizar estado e deduplicação de evento.
- A câmera mudar/focar defensor, tela de escudo, texto “X usou Y!” e retorno de câmera são pistas de **fase de carregado**. Mudança de câmera sozinha não prova golpe/custo; texto confirmado + eventos associados permitem descontar e voltar à contagem de ágeis.
- Troca de inimigo: preservar energia e HP individuais, moves, histórico, percentuais de confiança; restaurar no retorno; **resetar estágios temporários de Ataque/Defesa ao sair de campo**, de acordo com regra PvP, sem apagar energia. Pokémon nocauteado é marcado inativo até fim da luta e não reassumido por OCR incorreto.
- Corrigir/aprender a partir dos resultados observados; matemática determinística tem precedência sobre probabilidade de comportamento. Evitar suposições de intenção (“vai usar carregado” significa **pode usar** se energia suficiente).

#### E. OCR/visão e máquina de estados da batalha (somente leitura)

**Telas pré-batalha/seleção de liga/equipe:** ler liga atual (Grande, Ultra, Mestra), variações como Megaedição/copas, limite PC 1500/2500/sem limite, nome da equipe, seleção recomendada/manual, sprites, espécies, CP e ordem dos 3 aliados. Esses componentes são NATIVOS e não se redesenham. Preparar time próprio e regra de ranking/moves, mas validar disponibilidade real do formato.

**Durante batalha, ROIs adaptativas por posição:** cabeçalho sup. esquerdo/direito (nomes, PC, shields e Pokébolas); HP nativo esquerdo/direito (preenchimento, cores, perdas e desmaio); dois cartões nativos aliados à direita (sprite, PC e HP); botões nativos carregados inferiores (nome, tipo, disponibilidade/preenchimento, eficácia exibida); centro (mensagens de ataques/status); contador superior (contexto e tempo); modais de escudo e troca forçada. Suportar proporções/resoluções diferentes e overlays externos sem confundir controles do PGSharp.

**Eventos observáveis:** “Sneasel usou Jogo Sujo!”, “BLOQUEADO!”, “ATAQUE AUMENTOU!”, “ATAQUE DIMINUIU!”, “DEFESA AUMENTOU!”, “DEFESA DIMINUIU!”, qualificadores “MUITO”, “SUPER EFICAZ!”, “NÃO É MUITO EFICAZ”, tela “Quer usar um escudo protetor?”, “AGORA NÃO”, contadores, modal “Quer trocar por um Pokémon novo?”, troca voluntária, nocaute, HP e opções de time. Associar sempre ator, alvo, timestamp/turno, confiabilidade, efeito do escudo e estágio exato quando suportado. Alterações de status podem ocorrer **mesmo se o carregado for bloqueado**; registrar golpe, shield, dano, modificador e resultado em eventos separados.
  
**FSM recomendada:** PRE_BATTLE → BATTLE_FAST → CHARGED_PENDING → SHIELD_DECISION → CHARGED_RESOLUTION → BATTLE_FAST; ramo FAINT → FORCED_SWITCH → BATTLE_FAST; SWITCH → sincronizar estado e atualizar matchup. Os contadores de escudo, substituição obrigatória e cooldown da troca comum são temporizadores DIFERENTES. Eventos OCR mantidos em frames consecutivos não devem ser contabilizados múltiplas vezes.

**Prioridades de leitura:** 1) golpe identificado/HP/shield/status/troca, 2) CP/espécie/energia/contadores e equipe, 3) metadados auxiliares. Sempre distinguir CONFIRMADO vs ESTIMADO vs INCERTO; permitir correção manual de moves sem apagar a energia corretamente acumulada. OCR observa elementos nativos; **sem comandos automáticos/toques nos controles do jogo**.

#### F. HP real, estimativa de dano, bônus e fontes

- **HP exato do aliado via tela de detalhes** quando disponível: captura do Houndoom PC 1499 mostrava **120/120 PS = 120 HP máximos**, 100%. Vincular espécie/forma/PC/IV/nível/identidade do indivíduo, não apenas nome da espécie, e aproveitar na seleção da equipe. HP em batalha = leitura do preenchimento × HP máximo conhecido, com tolerância de pixels/arredondamento; calibrar por perdas sucessivas.
- Observação do overlay em outra captura: **Houndoom HP: 35**, **Sealeo HP: 245**; valores não devem ser assumidos exatos só por aparecerem. Se 245 for HP real do Sealeo, **provável inconsistência** com limites válidos da espécie/nível; investigar associação ao Pokémon errado, stats desatualizados, OCR, unidades e fórmula; validar contra Game Master atual.
- Para inimigos com CP conhecido e IVs/nível desconhecidos, gerar candidatos de nível+IV (e forma) compatíveis, obter faixa de HP máximo e inferir HP atual pela barra. Não afirmar HP exato por CP isolado; exemplos anteriores com “Raichu 1499 ~104–118 HP” são **ilustrativos e devem ser revalidados** com stats, níveis/CPM e forma atuais. Valores “ginásio/reides” na tela de golpes (por exemplo Raichu Trovão de Choques, Soco Trovoada, Desbravar) NÃO são diretamente o dano PvP nem o custo de energia PvP.
- Fórmulas básicas de referência, sob dados vigentes do jogo: HP_max = floor((Stamina_base_GO + IV_stamina) × CPM_nível), com piso aplicável; CP = floor[(Atk_base+IV_A) × sqrt(Def_base+IV_D) × sqrt(Stamina_base+IV_S) × CPM² / 10]. Usar níveis, formas, CP multipliers, buffs e tabelas atuais; não copiar HP-base das gerações principais como se fosse HP no GO.
- Dano PvP exige poder PvP, Ataque efetivo do atacante, Defesa efetiva do defensor, bônus STAB (~1,2), eficácia de tipos (por multiplicadores oficiais; supereficaz ~1,6; pouco eficaz ~0,625), Sombroso/clima/condições aplicáveis e estágios de Ataque/Defesa (aprox. +1 ×1,25; +2 ×1,5; −1 ×0,8; demais via fórmula oficial), com floor/arredondamento do jogo. **Poder base 50 ou 60 mostrado fora da aba PvP NÃO equivale a −50/−60 HP.** Clima geralmente não modifica dano em batalhas de treinador GBL: validar modo antes de aplicar “bônus do momento”.
- Comparar dano observado (ΔHP) com dano previsto, atualizar intervalos de estatísticas possíveis e confiança; não inferir um ágil apenas de cada variação da barra. Preview nº 18: faixas de dano/HP com escudo e sem escudo, aviso possível KO, plano de mitigação; nº 17 percentual de HP com origem clara. Mostrar “aprox.” e confiança na previsão.
- Diferenciar matchup e eficácia do golpe específico: ✓ verde/○ neutro/× vermelho refere-se à avaliação do confronto; chips de tipos/círculos individuais mostram eficácia dos golpes adversários e indicadores nos carregados do próprio jogador são nativos e apenas lidos.

**Fontes fornecidas para auditoria de HP/CP/moves e comentários (verificar qualidade/data, não adotar fórmulas antigas sem revisão):**
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/LnqUPTcmfI
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/NUhsqaEbna
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/SsREfbHowZ
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/96212SI7pC
- Reddit curto: https://www.reddit.com/r/TheSilphRoad/s/hzvOylwaIB
- Bulbapedia fornecida: https://bulbapedia.bulbagarden.net/wiki/List_of_Pok%C3%A9mon_by_base_stats_(Generation_VI-present))_(as_%60HP%60 (URL a normalizar/conferir; stats dos jogos principais ≠ base de GO).
- Planilha histórica: https://docs.google.com/spreadsheets/d/1xZ1daunztOgvOZxO7ABfJPF41-2mqMDzY1Qs_0OWwq8/edit#gid=399231321 ; abas: Pokemon CP/HP, Leveling Info, Go Base Stats, Type Advantage Chart, Move sets, Pokemon Science 2.0 data e outras. São dados históricos, NÃO Game Master atual.
- https://pokemongo.gishan.net/o/hp
- https://db.pokemongohub.net/tools/cp-calculator
- Referências históricas listadas na própria planilha: Reddit “how hp/maxstamina is calculated”, “exact pokemon cp formula”, “pokemon hp tier list” etc. Não afirmar ter lido todos os comentários de links curtos quando inacessíveis; validar diretamente antes de citar achados como fatos.
- Bancos por versão com Game Master GO + snapshot PvPoke, tipagem/forma/IV/CPM e data de atualização, com testes e licenças.

#### G. Recomendações de UX/QA e estados de entrega

**Fluxo de HUD preferido:** sempre visível e compacto apenas matchup principal, chips de tipos aprovados, monitor de golpes/energia, conselho tático, badges de reservas e (futuramente) três slots inimigos; mostrar PREPARE-SE/ESCUDO/KO/dano como alertas transitórios; listas de movimentos, telemetria, OCR confidence/histórico, cronômetro e configurações apenas sob demanda. Durante modais NATIVOS de escudo e escolha forçada, recolher menus grandes para não encobrir botões; permitir que o jogo mantenha todos os toques.

**Critérios de aceitação futuros:**
1. Diferenciar visualmente componentes GO × PGSharp × PvPPokeGo em capturas, sem alterar região nativa.
2. Estados olho/cadeado/ferramenta e modo edição funcionam dentro e fora de batalha, com escala/posição/opacidade persistidos.
3. Matchup ativo 3 estados, chips de tipo e eficácia inimiga nº 15 corretos em trocas.
4. Monitor ágil+2 carregados utiliza energia compartilhada, custo e geração PvP corretos, dupla carga, desconto residual e deduplicação.
5. Pokémon adversário saiu e voltou: mantém energia/HP/moves; buffs/debuffs resetam em troca; após KO ele fica desmaiado.
6. Reconhecimento de tela de escudo, OCR de golpe, BLOQUEADO, buff/debuff, tela de troca, contadores e câmera, com confiança/estados coerentes.
7. Novo painel de três inimigos começa ???, revela sem swap de identidade, tem 0/1/2 brilhos matematicamente possíveis, ativo/reserva/desmaiado e mantém posição fixa.
8. HP do aliado exato vindo de ficha quando conhecido; inimigo por faixa; rejeitar HP físico impossível; avaliar exemplo Houndoom 120/120 e Sealeo 245 suspeito.
9. Preview de dano/sobrevivência com ranges; sem confundir poder de golpe, energia e HP, nem aplicar modificadores PvE ao PvP.
10. Verificar legibilidade e ausência de sobreposição em aparelho Android/Samsung, modal de escudo, escolha de equipe, troca forçada, configurações PGSharp presentes e diferentes resoluções.
11. Não remover funções existentes, não reintroduzir cards de reserva grandes nem UI carregada; comparar com referência visual 0.5.17.
12. Escrever testes unitários de estado/energia/dano + testes com sequência de screenshots/vídeo reais; não marcar como concluído só pelas capturas.

**STATUS EM 2026-10-08:** documentação de requisitos visuais, mecanismos e sugestões = **CONCLUÍDO (neste guia)**. **Implementação, teste de regressão, APK e QA visual no Samsung para os novos requisitos = NÃO VERIFICADOS/PENDENTES**. A entrega técnica anterior 0.5.29 permanece o último build conhecido no histórico, sem confundir este adendo com novo build. Não alterar código/Gradle/versionCode/workflows nem sincronizar APK sem pedido específico.

**PRÓXIMO PASSO:** ler Guia Mestre/Checklist em release/0.5.29 e auditar as implementações existentes contra esta lista, classificando cada função como EXISTENTE-VALIDADA / EXISTENTE-COM-FALHA / PARCIAL / NOVA / BLOQUEADA. Implementar apenas deltas necessários, testes e documentação incremental na branch ativa, preservando visual e recursos anteriores. Somente depois compilar/QA físico/publicar artefato real e registrar commit, SHA-256 e link.



### 2026-10-08 — Inventário complementar dos MENUS: menu rápido na batalha + configurações da tela inicial (0.5.29)

**Origem:** sequência de capturas de tela do usuário da versão 0.5.29, seguida de inventários textuais aprovados durante a conversa. Este trecho complementa (não substitui) o adendo anterior de 2026-10-08 sobre a interface de batalha, energia e OCR. **Estado de evidência: identificado VISUALMENTE; não confundir controle visível/toggle ligado com função tecnicamente validada no APK.** Nenhum código foi alterado como consequência desta especificação.

**HEADs vistos antes da edição documental:** release/0.5.26 = `550fa08c8be1beb20bce4884d3a4c8c650c1dd62` e release/0.5.29 = `df26f33ed7dbdc6282eec5e65b9d77a0071aba52`. Branch default `main` não é a release ativa. A entrega técnica está identificada como 0.5.29 nas capturas. Atualizar as duas cópias do guia para evitar perda de contexto, mantendo 0.5.26 como referência histórica e 0.5.29 como continuação ativa. Estes SHA são PRÉ-COMMIT e deverão ser substituídos, na conferência final, pelos HEAD novos.

#### H. Menu rápido ABERTO sobre o Pokémon GO (HUD / batalha; inventário completo observado)

**Acesso:** pelo controle do PvPPokeGo (chave inglesa/engrenagem). O menu pode aparecer sobre a tela normal do mapa fora de batalha e deve manter o Pokémon GO completamente funcional. Capturas mostram segmentos diferentes da mesma lista rolável e visualização com opções avançadas expandidas/recolhidas. Não confundir o ícone de estrela **da opção textual no nosso MENU** com a **estrela amarela do PGSharp sobre a cena de batalha**, que NÃO é nossa.

| Grupo | Ícone observado | Rótulo literal / estado | Contrato funcional para auditar |
|---|---|---|---|
| BATALHA | ◉ | Ocultar HUD | Esconde HUD de batalha e mantém mecanismo seguro de recuperação/mostrar. Diferenciar da opção global Overlay no app. |
| BATALHA | ★ | Modo batalha assistido | Alterna recomendações táticas e alertas visuais; NÃO envia cliques/comandos ao Pokémon GO. |
| BATALHA | △ | Liga: Automático | Alterna/seleciona modo de liga; respetivo valor deve sincronizar com escolha da tela inicial. |
| BATALHA | ↔ | Editar posições do HUD | Abre modo de reposicionamento do HUD e controles de edição; não mover controles nativos. |
| HUD / VISUAL | T | Textos do HUD | Alternar legendas/textos visíveis do HUD, respeitando modo somente ícones. |
| HUD / VISUAL | S | Escala 80% ou 90% (observada) | Ajuste de escala; dois valores observados em capturas diferentes, não duas opções distintas. Persistir mudanças. |
| HUD / VISUAL | ◐ | Opacidade 90% ou 100% (observada) | Ajuste de opacidade; valores variáveis, não duplicatas. Persistir mudanças. |
| HUD / VISUAL | ↺ | Restaurar posições/layout | Restabelecer layout padrão com confirmação adequada e possibilidade de recuperar backup; evitar reset acidental. |
| ANÁLISE | A | Reconhecimento automático | Liga/desliga pipeline de visão/OCR sem alterar o jogo. |
| ANÁLISE | ◇ | Fraquezas do inimigo | Chips/indicadores dos tipos e suas fraquezas no topo do overlay. |
| ANÁLISE | ✓ | Indicador Pokémon atual | Avaliação do confronto do Pokémon ativo (melhor, neutro, evitar) com 3 estados. |
| ANÁLISE | 2 | Analisar reservas | Avaliação dos dois aliados de reserva, separada dos cards nativos. |
| ANÁLISE | T | Tipos das reservas | Tipos/ícones dos aliados reservas; não modificar sprites/cards originais. |
| ANÁLISE | ⚔ | Ataques do inimigo | Monitor com golpe ágil, até dois carregados e seleção manual/OCR. |
| ANÁLISE | •• | Contagem de carregados | Exibir pontos/cargas/contagem para ataques do inimigo; sinônimo potencial de “Pontos de carregamento” na tela inicial; validar se a configuração compartilhada é a mesma. |
| ANÁLISE | ⚡ | Progresso de energia | Energia residual prevista por Pokémon adversário, duas camadas visuais no carregamento e conservação após troca. |
| ANÁLISE | HP | HP e previsão de dano | HP conhecido/estimado e prévia de dano provável, respeitando incerteza e estado de escudo. |
| ANÁLISE | ↻ | Timer de troca | Cronômetro de cooldown de troca, separado de temporizador de escudo e troca forçada. |
| ANÁLISE | H | Histórico do inimigo | Informação resumida das observações/moves/energia dos inimigos; distinta do Histórico de batalhas completo na tela inicial. |
| ANÁLISE | D | Diagnóstico / DEBUG | Telemetria técnica / OCR para depuração (sem poluir interface padrão). |
| EXPANSÃO | ••• / ‹ | Mostrar opções avançadas / Ocultar opções avançadas | **UM** controle de dois estados; expande/recolhe opções do menu e modifica quais grupos ficam visíveis. |
| SISTEMA | ● | Gravar tela com PvPPokeGo | Capturar gravação interna com permissões adequadas, sem interferir na captura usada para OCR. |
| SISTEMA | Ⅱ | Pausar visão/análise | Congela/pausa processamento; deve distinguir de ocultar HUD, remover overlay e parar serviço. |
| SISTEMA | × | Remover sobreposição | Retira somente overlay sem fingir que todo serviço foi interrompido; funcionalidade real a validar. |
| SISTEMA | ■ | Parar serviço PvPPokeGo | Interrompe serviço e recursos/captura necessários; diferente de remover apenas a HUD. |
| SISTEMA | ▦ | Modo somente ícones | Oculta legendas/textos extensos mantendo ícones corretos. |
| SISTEMA | × | Fechar menu | Fecha apenas menu rápido, não encerra app ou batalha. |

**Contagem do inventário do MENU:** 4 (Batalha) + 4 (HUD/Visual) + 12 (Análise) + 1 (controle de expansão com dois estados) + 6 (Sistema) = **27 opções/controles distintos** nas capturas combinadas; não necessariamente todos aparecem simultaneamente devido a rolagem, modo expandido e contexto. **Elementos auxiliares do próprio HUD** também vistos com menu aberto: olho ocultar/mostrar e cadeado travar/destravar, além do status HP% e marcador `?`; não contabilizar esses como novas opções da lista. O ícone cadeado VERDE circulado pelo usuário representa o estado do CONTROLE DO PvPPokeGo, e não um ícone original do Pokémon GO.

**Comportamentos especiais e erros a evitar:** não atribuir “fechar aplicativo” ao × de “Fechar menu”; não confundir remover overlay com parar serviço; não trocar a arte oficial delicada do olho/cadeado por emojis; preservar áreas de toque associadas aos próprios ícones, sem hitboxes invisíveis distantes; menu deve ser exibido e editável com ou sem batalha; controles não devem cobrir o toque no jogo; exportação de posições antes de “restaurar layout”.

#### I. Tela INICIAL do aplicativo PvPPokeGo 0.5.29 — inventário integral das quatro capturas

**Título/descrição visível:** PvPPokeGo — “Assistente visual de batalhas PvP”; “Analisa a tela e mostra informações em tempo real sem enviar comandos ao Pokémon GO”. A tela mostra status “Pronto para iniciar o HUD”, instrução resumida “HUD: 🔓 mover • 🔒 travar • ◉ mostrar/ocultar • ⚙ menu rápido”, pasta interna de gravações `Movies/PvPPokeGo` e versão **0.5.29**. O texto de apresentação é a intenção de funcionamento: ausência de comandos automáticos ao jogo permanece regra inviolável.

**Seção BATALHA — 4 botões visíveis:**

| Controle | Texto | Função pretendida / validação necessária |
|---|---|---|
| 1 | Iniciar | Iniciar serviço/HUD, obter permissões necessárias e sincronizar toggles sem iniciar duas sessões em duplicidade. |
| 2 | Parar | Encerrar serviço/HUD; verificar diferenças em relação a Pausar ou Remover sobreposição. |
| 3 | Gravar | Começar/parar gravação local conforme estado real; revisar permissões e localização `Movies/PvPPokeGo`. |
| 4 | Nova batalha | Inicializar novo registro cronológico e estado de combate, com política explícita para sessão anterior. |

**Seção ASSISTÊNCIA E HUD — 3 toggles:**

1. **Overlay** — liga/desliga camada sobre outras aplicações; nas capturas aparece LIGADO (roxo).
2. **Reconhecimento automático** — liga/desliga reconhecimento visual/OCR; na captura aparece LIGADO.
3. **Modo Batalha Assistido** — mostra previsões/recomendações “CONTINUE FAST”, “TROQUE”, “ESCUDO”, “NÃO ESCUDE” e alerta de Charged, **sem tocar/enviar comandos** ao Pokémon GO; na captura aparece LIGADO.

**Seção LIGA E ANÁLISE — 4 botões de seleção de liga + 11 toggles:**

**Seleção “Liga / ranking PvP”:** `Automático` (selecionado na captura) / `Liga Grande` / `Liga Ultra` / `Liga Mestra`. Texto de status: “Automático: ler o lobby. Última detectada: Liga Grande”. A seleção manual/automática deve sincronizar com a opção `Liga: Automático` do menu rápido. Identificação de copas/edições especiais exige regra própria, não inferir só por PC.

| # | Toggle literal da tela inicial | Status visual no print | Mapeamento desejado ao menu rápido |
|---|---|---|---|
| 1 | Fraquezas do inimigo no topo | LIGADO | “Fraquezas do inimigo” |
| 2 | Indicador do Pokémon atual | LIGADO | “Indicador Pokémon atual” |
| 3 | Analisar reservas | LIGADO | “Analisar reservas” |
| 4 | Mostrar tipos das reservas | LIGADO | “Tipos das reservas” |
| 5 | Ataques do inimigo | LIGADO | “Ataques do inimigo” |
| 6 | Pontos de carregamento | LIGADO | “Contagem de carregados” — confirmar flag compartilhada |
| 7 | Progresso de energia | LIGADO | “Progresso de energia” |
| 8 | HP e previsão de dano sem escudo | LIGADO | “HP e previsão de dano” — texto pode ser mais completo no menu rápido |
| 9 | Timer de troca | DESLIGADO | “Timer de troca” |
| 10 | Modo edição — segure e arraste | DESLIGADO | “Editar posições do HUD” — verificar semântica/segurança do bloqueio |
| 11 | DEBUG | DESLIGADO | “Diagnóstico / DEBUG” |

Textos de ajuda nas capturas:
- Energia: “A previsão inimiga usa turnos de 0,5 s, energia residual e hipóteses de Fast Move. Quando há incerteza, mostra faixa/READY? em vez de inventar um valor exato.”
- HP: “Mostra HP estimado e uma faixa fantasma sobre a barra quando um carregado inimigo pode chegar. Dano observado tem prioridade; sem amostras, usa o modelo offline PvPoke como estimativa.” A parte final da descrição aparece parcialmente cortada nas capturas, então auditar implementação antes de ampliar a promessa.

**Seção APARÊNCIA / PERSONALIZAÇÃO — 2 sliders dentro de LIGA E ANÁLISE/aba inicial:**

1. `Escala: 80%` — no exemplo, controle deslizante; outras capturas do menu rápido exibem 90%. A escala é ajustável, não duas funções separadas.
2. `Opacidade: 90%` — slider; outros prints do menu rápido exibem 100%. A opacidade é ajustável, não duas funções separadas.
Ambos devem persistir, exportar/importar e atualizar imediatamente o menu rápido; limites reais exigem auditoria.

**Seção BACKUP E DIAGNÓSTICO — 3 ações visíveis:**

1. `Exportar` (configurações e posições): salva toggles/opções, escala, opacidade e posições personalizadas de ícones, blocos e menus do HUD. Evitar perda de posições já aprovadas.
2. `Importar` (configurações e posições): restaura um backup; texto original informa que **o layout fica travado por segurança ao importar**. Verificar versão de esquema/backward compatibility antes de sobrescrever.
3. `Exportar diagnóstico (.zip)`: pacote contendo configurações, posições, histórico, confiança/origem dos eventos e dados do aparelho. Consentimento e minimização de dados importantes ao compartilhar.

**Seção HISTÓRICO DE BATALHAS — 3 controles visíveis:**

1. `Atualizar`: atualizar/recarregar a lista.
2. `Exportar ZIP`: exportar histórico quando houver batalhas. Formatos anunciados no próprio texto: **.txt legível, .json completo e .csv em tabela**.
3. `Limpar histórico`: apagar registros, idealmente confirmar ação destrutiva. Na captura, exportar e limpar aparecem **desabilitados**, com “Nenhuma batalha registrada ainda”.
- Modelo de apresentação especificado: **conversa cronológica** com balões separados para “você”, “inimigo” e “assistente”; validar ligação aos registros reais antes de alegar funcionalidade.

**Seção SOBRE — informações, não controles configuráveis:**
- Crédito exibido no app: “Desenvolvimento PvPPokGo by: LucianoToscano”; contato de suporte **(84) 99131-9021**; versão 0.5.29.
- Texto: “Base visual V03 preservada. O HUD protege a interface nativa do Pokémon GO, permite reposicionar blocos no modo edição e mantém o botão de olho no topo.”
- Conferir ortografia e consistência entre marca `PvPPokeGo` e créditos `PvPPokGo`, sem remover crédito do desenvolvedor.

**Contagem da TELA INICIAL:** 4 (Batalha) + 3 (Assistência e HUD) + 4 (Seleção da liga) + 11 (Ligações de análise) + 2 (Escala/Opacidade) + 3 (Backup e diagnóstico) + 3 (Histórico) = **30 controles/opções** visíveis nas capturas, além de textos de descrição e seção “Sobre”. Valores ou estados roxos/cinzas são evidência visual da captura, NÃO teste automatizado de toggle nem garantia de persistência.

#### J. Regra de sincronização dos 2 caminhos de configuração e checklist de QA

- **Uma única configuração persistida por funcionalidade**, independentemente do caminho (tela inicial versus menu rápido). Ajustar escala/opacity, liga, reconhecer automático, modo assistido, fraquezas, matchup ativo, reservas, tipos, ataques, carga, energia, HP/previsão, timer, edição e DEBUG em um lugar deve refletir no outro sem reiniciar tudo.
- Normalizar rótulos inconsistentes SEM mudar semântica: `Pontos de carregamento` ↔ `Contagem de carregados`; `HP e previsão de dano sem escudo` ↔ `HP e previsão de dano`; `Reconhecimento automático` aparece nas duas interfaces; `Editar posições do HUD` ↔ `Modo edição — segure e arraste`; `Fraquezas do inimigo no topo` ↔ `Fraquezas do inimigo`.
- Garantir diferenciação de estado e comandos: `Overlay` global ≠ `Ocultar HUD` temporariamente ≠ `Modo somente ícones` ≠ `Pausar visão/análise` ≠ `Remover sobreposição` ≠ `Parar serviço`. Mostrar a ação correta e não quebrar o fluxo de retomada.
- Na interface inicial, botões `Iniciar`, `Parar`, `Gravar`, `Nova batalha` devem refletir estado real e permissões; evitar botões semanticamente ativos mas inoperantes. História/diagnóstico devem oferecer exportações realmente legíveis; exportar/limpar desabilitados quando não houver histórico.
- Ao importar backup de configurações, manter locks seguros e reconstituir posições tanto fora como dentro da batalha; não deslocar olhinho/cadeado/engrenagem. Validar formato e migrations em upgrades; restaurar só com ação explícita.
- Funcionalidade desligada no menu inicial não deve reaparecer por padrão no HUD; e configuração escondida por “Opções avançadas” não deve perder valor ao recolher menu.
- Respeitar a distinção já documentada entre **controles externos do PGSharp**, **elementos nativos do Pokémon GO somente para leitura**, e controles desenhados pelo próprio PvPPokeGo.
- Para QA: capturar (1) tela inicial inteira com scroll e modos on/off, (2) menu rápido completo e recolhido, (3) transição para edição/layout, (4) backup/restauração com posições personalizadas, (5) OCR em batalha real, (6) comportamento quando outro overlay está presente, (7) exportação do histórico com e sem registros; comparar resultado com prints antes de considerar “funcionando”.

**STATUS:** este inventário/contrato visual está **REGISTRADO NO GUIA**, mas a auditoria dos toggles, sincronização, testes de Android e APK com novas correções **NÃO FOI FEITA NESTA ETAPA**. Este commit é SOMENTE de documentação; não é build/release nova. Toda mudança futura nos menus deve gerar novo registro cronológico no guia da branch ativa e (quando relevante) no espelho histórico solicitado, incluindo branch/HEAD real, arquivos alterados, teste, pendências e links de artefatos verificados.



### 2026-10-15 — Início da arquitetura híbrida de dados (etapa isolada)

Versão-base: 0.5.29
Branch de trabalho: `feature/hybrid-data-sync-0.5.29`
Checkpoint de documentação: `faca18c838d8e8be962b7afcf4b97e013f291a2b`

OBJETIVO/PROBLEMA:
- Atualização diária automática via Wi-Fi, mantendo o motor PvP totalmente offline durante a batalha.

ALTERAÇÕES:
- Auditados o empacotador PvPoke, checklist da 0.5.29 e dependências Android.
- Criado armazenamento isolado de snapshots com verificação SHA-256, staging e ativação explícita fora de batalha.
- Criados testes JVM de checksum, ativação e segurança de caminhos.
- Especificação e limites em `docs/HYBRID_DATA_SYNC_2026-10-15.md`.

ARQUIVOS PRINCIPAIS:
- `app/src/main/java/com/lucianotoscano/pvppokego/data/sync/HybridSnapshotStore.kt`
- `app/src/test/java/com/lucianotoscano/pvppokego/data/sync/HybridSnapshotStoreTest.kt`
- `docs/HYBRID_DATA_SYNC_2026-10-15.md`
- `docs/PROJECT_CONTINUITY_GUIDE.md`

TESTES:
- Testes criados, ainda não executados; compilação Android e APK não realizados.

RESULTADO:
- Base inicial de staging adicionada sem tocar no motor de batalha nem substituir assets existentes.

STATUS:
PARCIAL.

PENDÊNCIAS:
- Implementar e conectar WorkManager (Wi-Fi, diário), manifesto confiável, downloader, preferências e carregador.
- Auditar ponto de troca de sessão, executar testes/build e validar em aparelho.

PRÓXIMO PASSO:
- Completar a integração no branch isolado; somente depois de testes promover à release.


### 2026-10-15 — Agendamento híbrido diário (continuação)

Versão-base: 0.5.29
Branch: `feature/hybrid-data-sync-0.5.29`
Commit de documentação anterior: `04cf42f4f6a34c1a57dedd8eb8cfa31c463fa63e`

ALTERAÇÕES:
- Dependência WorkManager 2.10.1 adicionada ao módulo Android.
- `HybridSyncScheduler` registra trabalho periódico diário em rede não tarifada, com opções persistidas de automático=true e wifiOnly=true.
- Inicialização no `PvPPokeGoApp`.
- Worker permanece deliberadamente sem download/ativação até existir manifesto confiável, validação de esquema e integração segura com sessão de batalha.

TESTES:
- Inspeção de código; build Android e testes JVM não executados nesta etapa.

STATUS:
PARCIAL — agendamento criado; atualização remota real e carregamento de snapshots ainda pendentes.

PENDÊNCIAS:
- Definir fonte HTTPS versionada e confiável; baixar e validar manifesto/arquivos; integrar `GameDataRepository` e troca entre batalhas.
- Corrigir/revisar qualquer problema apontado pelo compilador, executar testes e gerar APK Standard apenas após validação.


### 2026-10-15 — Downloader HTTPS e integração do carregador (continuação)

ALTERAÇÕES:
- Worker diário lê manifesto HTTPS configurável via `pvppokego.hybridManifestUrl`; padrão vazio = nenhum download.
- Implementadas restrições de rede, host, redirecionamento, tamanho, esquema JSON, mínimos de registros e SHA-256.
- Downloads ficam em staging. Ativação de base pendente somente em inicialização de novo processo Android.
- `GameDataRepository` lê snapshots ativados com fallback para assets do APK.
- Detalhes em `docs/HYBRID_DATA_SYNC_2026-10-15.md`.

STATUS: PARCIAL / NÃO VALIDADO.
- Não existe manifesto público confiável configurado; atualização real ainda depende da publicação do feed.
- Compilação Android, testes, assinatura e APK NÃO executados.
- Pendente auditar possíveis falhas de compilação e de troca/rollback, executar CI, validar aparelho Samsung e assinar APK.
- Nenhuma alteração promovida à release/0.5.29.


### 2026-10-15 — Robustez da integração híbrida e verificação de CI

- `GameDataRepository` fixa a pasta do snapshot no início de cada `load`, impedindo mistura de versões durante carregamento.
- Se o Game Master ativo estiver ausente/inválido/vazio, o apontador de snapshot é desativado e o Game Master embarcado é recuperado.
- `HybridSnapshotStore` não considera ponteiro ativo válido sem diretório READY e oferece rollback para os assets embarcados.
- Teste unitário adicional para rollback.
- Consulta de workflows de GitHub Actions para o commit de integração: nenhum workflow retornado.
- Ambiente de execução local sem Gradle/Android SDK; testes Android e APK não realizados.
- Sem URL de manifesto de atualizações verificada/configurada: o worker continua no modo seguro sem downloads.
- **STATUS: IMPLEMENTAÇÃO PARCIAL, NÃO PRONTA PARA RELEASE.** Não promover PR #37 sem compilação, testes e validação real no aparelho.


### 2026-10-15 — Pipeline de validação híbrida

- Criado `.github/workflows/hybrid-data-sync.yml` para executar testes unitários Standard, montar APK debug, verificar ZIP e gerar SHA-256 como artefato.
- Verificação de workflows do commit: nenhuma execução confirmada pela API disponível.
- Não houve build local: ambiente sem Android SDK/Gradle e acesso de rede externo indisponível.
- Nenhum APK novo validado. PR #37 permanece draft.
- Para finalizar: executar pipeline real, corrigir eventuais falhas de compilação/testes, configurar/publicar feed HTTPS confiável, validar integridade do manifesto e executar QA no dispositivo.
- Não promover mudanças para a branch de release sem essas evidências.


### 2026-10-15 — Espelhamento público da arquitetura híbrida

- Repositório público verificado: `LucianoToscano/PvPPokeGo-APK-Build` (branch estável `main` preservada).
- Criada branch pública `preview/hybrid-data-sync-20261015`.
- Espelhados `HybridSnapshotStore.kt`, `HybridSyncScheduler.kt`, testes, `GameDataRepository.kt`, `PvPPokeGoApp.kt` e `app/build.gradle.kts`.
- Workflow público `.github/workflows/build-standard-apk.yml` atualizado NA BRANCH PREVIEW para disparar em push e aceitar a compilação híbrida.
- README da branch pública registra caráter experimental e falta de validação.
- Consulta dos runs para o commit público `0c4d92cea47139f6ebc0ff1c439c6201d1ac3555`: nenhum workflow retornado pela API.
- Nenhum APK novo verificado, nenhuma assinatura/teste Android confirmados; não afirmar conclusão ou promover para main.
- Fonte HTTPS do manifesto ainda sem URL publicada e confiável.


### 2026-10-15 — CONSOLIDAÇÃO CANÔNICA: arquitetura híbrida e espelho público

**Escopo e preferências aprovadas pelo usuário**
- Projeto Android nativo PvPPokeGo; evolução incremental sem remover HUD, OCR, reconhecimento aliado/reservas, ícones, histórico, motor matemático ou assets.
- Atualização automática: **ativada**; download: **somente rede não tarifada (preferência Wi-Fi)**; frequência: **diária**. WorkManager não garante horário exato nem transporte Wi-Fi especificamente.
- Em batalha: **nenhuma consulta de rede nem troca de versão de dados**; o app deve continuar funcionando offline.

**Repositórios e isolamento**
- Canônico: https://github.com/LucianoToscano/PvPPokeGo
- Base preservada: `release/0.5.29`; desenvolvimento: `feature/hybrid-data-sync-0.5.29`; revisão isolada: https://github.com/LucianoToscano/PvPPokeGo/pull/37 (draft).
- Espelho público de build: https://github.com/LucianoToscano/PvPPokeGo-APK-Build
- Branch pública experimental: `preview/hybrid-data-sync-20261015`; **não modificar `main` até aprovação de CI e QA**.
- O espelho público é uma cópia de código, não uma sincronização automática nem substituto da fonte canônica; nunca publicar credenciais, keystores ou dados privados.

**Arquivos criados ou modificados no repositório canônico**
- `app/src/main/java/com/lucianotoscano/pvppokego/data/sync/HybridSnapshotStore.kt`: staging SHA-256, validação de nomes, ativação fora de batalha e fallback.
- `app/src/main/java/com/lucianotoscano/pvppokego/data/sync/HybridSyncScheduler.kt`: WorkManager diário, conexão não tarifada, preferências, downloader HTTPS e manifesto.
- `app/src/test/java/com/lucianotoscano/pvppokego/data/sync/HybridSnapshotStoreTest.kt`: testes escritos para hash, traversal, ativação e rollback.
- `app/src/main/java/com/lucianotoscano/pvppokego/data/GameDataRepository.kt`: seleção de snapshot local por carregamento e fallback para assets.
- `app/src/main/java/com/lucianotoscano/pvppokego/PvPPokeGoApp.kt`: inicialização do agendador e ativação de snapshot pendente no início de novo processo.
- `app/build.gradle.kts`: WorkManager e propriedade opcional `pvppokego.hybridManifestUrl`.
- `.github/workflows/hybrid-data-sync.yml`: testes Standard, APK QA debug, verificação ZIP e SHA-256.
- `docs/HYBRID_DATA_SYNC_2026-10-15.md`: especificação técnica e checkpoints.
- Este `docs/PROJECT_CONTINUITY_GUIDE.md`: registro cronológico.

**Arquivos alterados no espelho público, apenas na branch preview**
- Cópia dos três arquivos de sincronização/teste e de `GameDataRepository.kt`, `PvPPokeGoApp.kt`, `app/build.gradle.kts`.
- `.github/workflows/build-standard-apk.yml`: habilitado build na branch preview e nomeação de artefato híbrido.
- `README.md`: aviso de branch experimental e ausência de validação.
- Último commit público de workflow consultado: `0c4d92cea47139f6ebc0ff1c439c6201d1ac3555`; API não retornou workflow associado.

**Situação comprovada / não comprovada**
- Códigos e commits foram criados remotamente; **não há evidência de testes JVM executados, compilação Android aprovada, APK instalável novo, assinatura validada ou QA em aparelho**.
- Nenhum manifesto remoto confiável foi publicado ou configurado. Com URL vazia, worker é no-op e não realiza atualização real.
- Verificação SHA-256 dos arquivos não equivale a autenticação criptográfica do manifesto; assinatura/pinning e validação de consistência cruzada ainda são pendências.
- O rollback ainda necessita de testes abrangentes de dados cruzados e ciclos de versão.
- **STATUS: PARCIAL, NÃO PROMOVER PARA RELEASE NEM DECLARAR CONCLUÍDO.**

**Próximas ações obrigatórias**
1. Conferir HEAD e diferenças nas duas branches de trabalho, revisar os pontos de compilação e schema dos dados reais.
2. Executar o workflow público/privado e ler logs; corrigir todos os erros até obter testes e build aprovados.
3. Publicar feed HTTPS confiável com manifesto versionado/autenticado, tamanhos, hashes e esquema correto; testar downloads, ausência de rede e falhas.
4. Validar transição entre batalhas, rollback completo, OCR/HUD/ícones e histórico em aparelho Android.
5. Verificar APK (assinatura, SHA-256, conteúdo, instalação) e só então considerar merge/release.

**Regra de continuidade**: consultar primeiro este Guia Mestre e o registro técnico; não reiniciar implementação, não sobrescrever a branch estável e registrar cada novo commit/resultado.


### 2026-10-15 — IMPLEMENTAÇÃO HÍBRIDA: BUILD APROVADO, FEED PÚBLICO E DRIVE (EVIDÊNCIAS)

**Fonte canônica e branches**
- Projeto canônico: https://github.com/LucianoToscano/PvPPokeGo , branch feature/hybrid-data-sync-0.5.29, revisão PR #37 (draft).
- Build público: https://github.com/LucianoToscano/PvPPokeGo-APK-Build , branch preview/hybrid-data-sync-20261015.
- Feed remoto público PUBLICADO na branch isolada hybrid-feed: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/tree/hybrid-feed
- Manifesto online VERIFICADO: https://raw.githubusercontent.com/LucianoToscano/PvPPokeGo-APK-Build/hybrid-feed/manifest.json
- Base release/0.5.29 e espelho público main não foram sobrescritos.

**Correções e implementação**
- Resolvido erro do workflow público que chamava apksigner com o nome antigo do arquivo.
- SnapshotStore agora persiste CHECKSUMS, revalida SHA-256 após reinicialização, rejeita corrupção/traversal, recupera staging interrompido, fixa a versão ativa e oferece rollback para assets embarcados.
- Worker HTTPS limita domínio e caminho à origem pública fixa, impõe limites de tamanho, 7 arquivos obrigatórios e JSON mínimo; agenda atualização diária (WorkManager) e exige Wi-Fi real caso wifi_only=true.
- Download é apenas staging; ativação somente em nova inicialização do processo, não durante uma batalha.
- GameDataRepository valida consistência de Game Master, rankings da liga e CPM antes de selecionar os dados remotos, com fallback para assets do APK.
- tools/generate_hybrid_feed.py prepara manifesto versionado por conteúdo; tools/test_generate_hybrid_feed.py testa determinismo, integridade do tamanho e rejeição de ranking inválido.
- Fluxo de build público publica dados validados para branch hybrid-feed após testes/compilação/assinatura. Inclui licença e provenance pvpoke_snapshot.json.
- Execução pública de CI 38801861207 APROVADA para testes do gerador; CI 38800587467 APROVADA para assinatura e publicação de dados.
- Publicação da branch hybrid-feed verificada no commit 83a9f717ef9f8b392416095708756d084eb31557, versão pvpoke-c6a8aa396f84df1914503921, 7 arquivos.
- Nova validação Android 35 via emulador foi adicionada à CI pública no commit 0d81f301fec925bef2b9745a3928bba879b48151; **resultado ainda não conhecido no momento deste registro**.

**APK e evidências**
- GitHub Actions 38798974370: primeiro build híbrido aprovado, arquivo SHA-256 5e9594cd76dc0779cd1a50753b6e414ab6aad0f5eab4ff3152020472decd5547.
- GitHub Actions 38800587467: pacote híbrido com feed padrão (QA debug) aprovado, artefato 12004582000.
- APK da segunda compilação extraído e verificado localmente por ZIP e hash SHA-256 3d6c0b8b44937f044cf91a0db87add7daff3dc832e778865ed5c83bc8e66ebee. apksigner: assinatura APK Signature Scheme V2 válida, uma assinatura debug.
- Cópia ZIP APK + SHA256SUMS + signature-verification enviada ao Drive do projeto:
  https://drive.google.com/file/d/18kM8F-wCIL3nMQPpEyk6z3X78y80gh3-/view?usp=drivesdk
  arquivo PvPPokeGo-0.5.29-Hybrid-QA-2026-10-15.zip, pasta id 1ybogVRRnaOe0KtWjlovxS56bJOi_JewI; upload e readback Drive confirmados.

**Limitações e regras de aprovação**
- CI e assinatura debug não comprovam funcionamento real de HUD/OCR/captura em aparelho físico; QA manual ainda necessária.
- Modelo atual de confiança HTTPS fixado em repositório Github + SHA-256 de payload NÃO é assinatura criptográfica independente do manifesto. Para lançamento produção seria ideal chave de assinatura gerida pelo dono e verificação da assinatura.
- A compilação tem assinatura de TESTES; instalação por cima de versão com assinatura diferente pode exigir desinstalar versão antiga, com risco de perda de dados locais. Exportar configurações antes.
- NÃO marcar PR #37 pronto para release nem mesclar à release/0.5.29 sem validação adicional em dispositivo.
- Próxima sessão: ler primeiro este guia, conferir HEAD nas duas branches, últimos GitHub Actions, run de emulador 38802346032 e depois registrar resultado real.


### 2026-10-15 — Smoke test de instalação Android 35 APROVADO

- Workflow público ampliado com job independente smoke usando reactivecircus/android-emulator-runner@v2 e APK publicado pelo job build.
- GitHub Actions 38802346032 terminou com sucesso: build/testes APK Standard e job Android 35 smoke verde.
- Teste smoke instalou o APK QA, iniciou a activity com adb, esperou e confirmou PID ativo. Screenshot capturado como artefato 12005831245. A captura ainda mostra diálogo normal de autorização de notificações; não equivale a teste completo da interface e do overlay.
- Caminho da imagem extraída nesta sessão: hybrid-smoke/launcher-smoke.png.
- Sem testes reais de visão, batalhas, overlays ou conexão Wi-Fi de aparelho físico; continuam pendentes.
- Outra execução final 38802898336 foi iniciada para verificar manifesto estável por conteúdo (exclui timestamp variável do snapshot) e não publicar bases repetidas sem mudança real nos dados/ licença.
- Não mesclar a branch canônica na release por este teste isolado.

### 2026-10-15 — FECHAMENTO DA BUILD HÍBRIDA: EXECUÇÃO FINAL 38802898336 (APROVADA)

**Ponto recuperado**: o trabalho estava interrompido durante `Unit tests and Standard debug APK` do GitHub Actions 38802898336, commit público 797330db330b0db887ccb89d90591dec6c6185db. Retomado diretamente desse run, sem reinício de projeto.

**Resultados verificados do GitHub Actions**
- Execução pública: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38802898336
- Conclusão: success, 2026-10-15 20:45:00 UTC. Jobs `build` e `Android 35 install and launcher smoke`: success.
- Passaram: teste Python do gerador de feed, empacotamento do banco PvPoke e atlas visual, `:app:testStandardDebugUnitTest`, `:app:assembleStandardDebug`, verificação ZIP, SHA-256, assinatura com apksigner, construção/publicação do manifesto e instalação/abertura no emulador Android 35.
- Emulador verificou instalação do APK com adb, inicialização de MainActivity e processo vivo; não testou batalha real, OCR/captura ao vivo ou HUD em aparelho físico.
- Último artefato GitHub: ID 12004839451, `PvPPokeGo-0.5.29-Hybrid-QA-debug`, ZIP de 36.157.916 bytes contendo APK, `SHA256SUMS.txt` e `signature-verification.txt`.

**Verificação independente do artefato**
- APK extraído do ZIP: `PvPPokeGo-0.5.29-Hybrid-QA-debug.apk` de 80.318.297 bytes.
- SHA-256 do APK: `97e0b919b6de2daf0c0211f59ceb75398fcf345cec55aaaf268c9d130a7c7ae5`, idêntico ao arquivo de checksum dentro do artefato.
- `unzip -t` passou sem erros. Relatório apksigner confirma APK Signature Scheme v2=true, 1 signatário; assinatura **debug**, não substitui assinatura definitiva de produção.
- Os dez recursos essenciais offline (Game Master, rankings 1500/2500/10000, CPMS, snapshot/licença PvPoke, reconhecimento visual e atlas de ícones) existem no APK.
- Hash SHA-256 e tamanho dos SETE arquivos do feed foram comparados aos arquivos embarcados no APK: todos correspondem.

**Feed remoto verificado**
- URL: https://raw.githubusercontent.com/LucianoToscano/PvPPokeGo-APK-Build/hybrid-feed/manifest.json
- Branch pública `hybrid-feed`, HEAD verificado `054fe923a722e5c87a7283d8a0f78cbdc22cb383`.
- Versão publicada no momento do fechamento: `pvpoke-6f188deab6e057566a6e0fca`, manifesto de 7 arquivos.
- Corrigido e testado gerador para NÃO mudar a versão somente por alteração no timestamp de `pvpoke_snapshot.json`; workflow de publicação não substitui o banco se a versão calculada não mudou. Essa lógica passou nos testes Python do run final.

**Arquivamento no Google Drive**
- Cópia final verificada do ZIP enviado à pasta `PvPPokeGo` (ID `1ybogVRRnaOe0KtWjlovxS56bJOi_JewI`), preservando os QA anteriores.
- Arquivo: `PvPPokeGo-0.5.29-Hybrid-QA-FINAL-2026-10-15.zip`.
- Link: https://drive.google.com/file/d/1qGpsg04zqNuNEU82yRVbBI7QSpmDh4Dy/view?usp=drivesdk
- ID Drive `1qGpsg04zqNuNEU82yRVbBI7QSpmDh4Dy`; tamanho de 36.157.916 bytes e pasta confirmados em leitura de metadados.

**Estado de segurança e regressões**
- Branch privada de desenvolvimento `feature/hybrid-data-sync-0.5.29` e branch pública `preview/hybrid-data-sync-20261015` preservadas; versão estável privada `release/0.5.29` e pública `main` não substituídas.
- PR privado #37 permanece draft; NÃO mesclar automaticamente por causa de QA debug.
- O app verifica HTTPS de repositório fixo, hashes SHA-256, estrutura de arquivos, Wi-Fi real quando configurado, ativação na próxima inicialização e rollback. A autenticação do MANIFESTO ainda não usa assinatura criptográfica independente; risco residual para produção.
- Pendências para lançamento oficial: QA de batalha/OCR/captura/overlay em dispositivo físico, política de atualização assinada com chave própria e geração de APK de release com assinatura permanente. Evitar desinstalar versão anterior sem exportar configurações, porque chaves de assinatura podem diferir.

**Ponto de continuidade**: finalizar QA físico e procedimento de lançamento assinado, sem refazer CI 38802898336 nem reexecutar o empacotamento que já foi aprovado. Aplicar futuras alterações primeiro na fonte canônica, depois no espelho público isolado, registrando cada mudança.


### 2026-10-15 — CICLO 0.5.30 Hybrid QA: código aplicado, build final em validação

**Solicitação:** implementar as pendências possíveis, gerar APK e ZIP com projeto editável, registrar tudo no Guia Mestre e distribuir pelo GitHub público e Drive, preservando versão estável.

**Branches preservadas:** canônica \`feature/hybrid-data-sync-0.5.29\` contra \`release/0.5.29\`; espelho público \`preview/hybrid-data-sync-20261015\` contra \`main\`. O projeto NÃO foi reiniciado e nenhuma branch estável recebeu merge.

**Recursos aplicados no código canônico e espelhados no público:**
- Nova versão experimental \`0.5.30\` / versionCode \`35\`.
- \`SettingsRepository.kt\`: controles de atualização híbrida diária/Wi-Fi e status da base ativa/pedente; preferência de sincronização incluída em backup/import JSON.
- \`SettingsScreen.kt\`: seção de dados híbridos com toggles e status, botão funcional para solicitar verificação manual, controle opcional de três slots inimigos.
- \`HybridSyncScheduler.kt\`: WorkManager one-shot manual com restrição de rede, mesmo com agenda diária desligada; continua respeitando Wi-Fi real quando selecionado.
- \`MainActivity.kt\`: ZIP de diagnóstico inclui \`hybrid_sync.txt\`.
- \`OpponentTeamTracker.kt\` (novo): roster por ordem de observação, máximo de três identidades confirmadas, CP/HP, estado ativo/desmaiado, placeholders desconhecidos; não inventa quarto slot; não adiciona slot fantasma se a mesma espécie está ambígua sem CP; permite upgrade OCR para forma exata com nome+CP confirmados.
- \`BattleEngine.kt\`: integra roster, mantém energia do inimigo associada ao slot reconhecido, restaura estado após troca e evita reutilizar HP de oponente anterior.
- \`OpponentTeamTrackerTest.kt\` (novo): cobre três identidades, retornos, duplicata com CP distinto, CP incerto e atualização OCR→forma.
- CI público \`.github/workflows/build-standard-apk.yml\`: gera APK QA debug e ZIP editável do projeto compilável; incorpora assets PvPoke offline e atlas e gera Gradle Wrapper 8.11.1, checksum e verificação ZIP.
- CI canônica \`.github/workflows/package-project-zip.yml\`: configurada para exportar ZIP completo do repositório privado pela feature branch; execução 38813504687 FAILED antes de produzir passos/logs de runner; não afirmar que ZIP canônico foi entregue.
- Matriz de verificação \`docs/FEATURE_AUDIT_0.5.30.md\`; issue de bloqueios externos #38: https://github.com/LucianoToscano/PvPPokeGo/issues/38.

**Evidências intermediárias verificadas:**
- CI pública 38813448792: build 0.5.29 com source artifact 12009507794 e APK, ambos criados; ZIP extraído e íntegro.
- CI pública 38814583388: job build de 0.5.30 CONCLUÍDO COM SUCESSO; artefatos APK 12009574157 e fonte editável 12010676577. ZIP de fontes extraído; arquivo interno checksum confere, Gradle Wrapper, MainActivity, icon atlas e OpponentTeamTracker estão presentes.
- Smoke emulator do run 38814583388 e CI mais recente 38815466466 (última correção de roster) ainda pendentes no momento deste registro. Não confundir artefato intermediário com último commit.
- CI pública 38813112040 falhou por YAML corrompido na primeira tentativa de inserir pacote fonte; workflow foi recuperado com base no último workflow válido. Nova CI 38813448792 passou.
- Source build pública inclui todos os arquivos necessários para compilar o Android 0.5.30 e editar telas/motor/dados, mas NÃO substitui o ZIP integral do repositório privado com históricos antigos. O git canônico completo permanece no repositório privado.

**Bloqueios externos e não concluídos:** QA em Samsung/Pokémon GO real, reconhecimento e HUD durante batalha, atualização em Wi-Fi físico, imagens/backgrounds finais aprovadas, assinatura de manifesto com chave privada guardada fora do GitHub, assinatura permanente de release e backend de IA remoto com consentimento. Não marcar essas atividades como realizadas por emulador. Não mesclar PR #37 na release sem cumprir os critérios.



### 2026-10-15 — ENTREGA REAL 0.5.30 HYBRID QA: APK + ZIP EDITÁVEL + DRIVE + CI

**Execução final verificada**: GitHub Actions público https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38815466466; commit \`d838a44a3bf6ded5110e82589f2961cc205f14af\`; execução e ambos os jobs **build** e **Android 35 install and launcher smoke** encerrados com **SUCCESS**. Corrigido o YAML de workflow inválido que causou falha no run 38813112040; o último workflow foi validado em build e instalação do emulador.

**Código alterado e integrado no build QA 0.5.30, versionCode 35**: \`SettingsRepository.kt\`, \`SettingsScreen.kt\`, \`MainActivity.kt\`, \`HybridSyncScheduler.kt\`, \`BattleEngine.kt\`, novo \`OpponentTeamTracker.kt\`, novo \`OpponentTeamTrackerTest.kt\`, \`app/build.gradle.kts\`, pipeline público \`.github/workflows/build-standard-apk.yml\`. Configuração de sincronização agora na tela e backup, botão de solicitação manual, diagnóstico da base e HUD opcional de três slots observados do adversário com CP/HP/energia/marcação sem inferir espécies não observadas. Testes incluem casos de duas instâncias da mesma espécie e CP desconhecido, evitando slot fantasma.

**Testes efetivamente verificados**: JUnit app \`:app:testStandardDebugUnitTest\`, \`:app:assembleStandardDebug\`, testes Python do manifesto, atlas/asset packaging, APK apksigner V2, ZIP e SHA-256; no emulador Android 35 o APK instalou, MainActivity abriu e processo ficou vivo. Não houve teste em batalha real Samsung, captura OCR dinâmica, sessão longa, atualização diária real Wi-Fi, nem QA estética em aparelho — continuar marcando como pendente.

**Entregáveis do CI final**:
- Artefato APK: ID 12009579817, \`PvPPokeGo-0.5.30-Hybrid-QA-debug\` (ZIP de artefato 36.170.637 bytes). APK extraído \`PvPPokeGo-0.5.30-Hybrid-QA-debug.apk\` 80.351.065 bytes, SHA-256 \`7823de5c280179369a6a25ebbb95c33546bb11b8538f1da430e177d94f2c2d4d\` — corresponde a SHA256SUMS.txt. \`unzip -t\` PASS; apksigner confirma v2=true, um signatário debug.
- Artefato fonte: ID 12009909537, \`PvPPokeGo-0.5.30-Hybrid-FONTE-EDITAVEL\`. ZIP editável extraído \`PvPPokeGo-0.5.30-Hybrid-FONTE-EDITAVEL.zip\` 6.280.245 bytes, SHA-256 \`47e73c1a966fcce7ced4a66e8310563aacc21a69750b83b67634a5f7dc974a46\` — corresponde a .sha256. Integridade ZIP PASS; inclui 129 arquivos reais entre código Kotlin, testes, \`gradlew\`, \`gradle/wrapper/gradle-wrapper.jar\`, Gradle build scripts, assets de Pokémon e dados PvPoke.
- Distribuição Google Drive **confirmada por metadata read-back** na pasta \`PvPPokeGo\` ID \`1ybogVRRnaOe0KtWjlovxS56bJOi_JewI\`:
  APK QA junto dos arquivos SHA/assinatura: https://drive.google.com/file/d/1cDZ_GHE_vMpjieFl2Al38wdUWYfmncz-/view?usp=drivesdk (ZIP externo de 36.170.637 bytes).
  Código Android editável atual: https://drive.google.com/file/d/1LTrMj6DaUTOiJgQr7TYqHQE71VGNfOln/view?usp=drivesdk (ZIP interno de 6.280.245 bytes).
- Extra local opcional, sem sobrescrever fontes atuais: pacote \`PvPPokeGo-0.5.30-PROJETO-EDITAVEL-COM-ARQUIVO-HISTORICO.zip\` (~7.183.126 bytes; SHA-256 \`8c303bdcc7a7e19173d2930c54650c53614c6219a18c5a036b2c3d9744fe11aa\`). Usa na raiz a fonte Android atual e numa subpasta isolada um histórico privado de 06/10/2026 recuperado do run canônico bem-sucedido 37525286145; o histórico é antigo e não pode ser confundido com o HEAD canônico atual. README interno registra proveniência/limitação. Esse pacote é entregue como arquivo de apoio local, não afirmar upload no Drive/GitHub sem verificação.
- Fonte da branch pública: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/tree/preview/hybrid-data-sync-20261015
- Fonte canônica privada: https://github.com/LucianoToscano/PvPPokeGo/tree/feature/hybrid-data-sync-0.5.29
- Mapa de funcionalidades pendentes e implementadas: \`docs/FEATURE_AUDIT_0.5.30.md\`.
- Questões físicas/credenciais ainda abertas: https://github.com/LucianoToscano/PvPPokeGo/issues/38. PR #37 segue rascunho.

**Delimitação importante**: a entrega QA do APK + ZIP Android buildável + GitHub público + cópias Drive está concluída e verificada. **NÃO** foi possível garantir produção, IA remota multiusuário, assinatura de manifesto independente, keystore permanente, nem comportamento visual durante batalhas em Samsung sem credenciais/hardware/testes externos. O arquivo ZIP do espelho público é integral e editável para compilar ESTE Android, mas não é snapshot integral e atual de TODOS os históricos/docs/branches do repositório canônico privado. Preservar as branches estáveis \`release/0.5.29\` e \`main\` e os backups anteriores.



### 2026-10-15 — Revisão adicional da entrega e validação visual de startup

- Confirmado por inspeção local: QA APK 0.5.30 SHA-256 \`7823de5c280179369a6a25ebbb95c33546bb11b8538f1da430e177d94f2c2d4d\`, size 80.351.065 bytes, Android Signature Scheme v2 true (debug).
- ZIP de fonte editável QA: SHA-256 \`47e73c1a966fcce7ced4a66e8310563aacc21a69750b83b67634a5f7dc974a46\`, 6.280.245 bytes, integridade ZIP PASS; inclui Gradle Wrapper, fontes, testes e assets offline.
- Pacote combinado (fonte 0.5.30 na raiz + acervo privado **histórico** em pasta isolada): \`PvPPokeGo-0.5.30-PROJETO-EDITAVEL-COM-ARQUIVO-HISTORICO.zip\`, 7.183.126 bytes, SHA-256 \`8c303bdcc7a7e19173d2930c54650c53614c6219a18c5a036b2c3d9744fe11aa\`, ZIP íntegro.
- Cópia desse pacote combinado **enviada ao Google Drive** nesta etapa e readback confirmado: https://drive.google.com/file/d/1TNVfXolp6YnFw5tjV9VE5gUDVcpa8hmk/view?usp=drivesdk, pasta do projeto id \`1ybogVRRnaOe0KtWjlovxS56bJOi_JewI\`. As outras cópias APK/source do run 38815466466 permanecem disponíveis separadamente no Drive.
- Revisão **visual** da captura de tela gerada pelo smoke do run 38815466466 mostrou aviso do Android "Pixel Launcher isn't responding" sobre o diálogo de notificações. Portanto, o smoke antigo só prova instalação/abertura/PID do app; **NÃO PROVA que a UI do PvPPokeGo estava visível sem interposição**. Não afirmar QA visual completo com base nele.
- CI público foi aperfeiçoado com verificação por \`uiautomator dump\` de texto da interface PvPPokeGo e rejeição de diálogos ANR/launcher e exceções app. Workflow commit público \`ce318c669f69c6b14d6a1a852140f1d1f4ebb05c\`. Resultado ainda em execução no momento desta anotação; marcar somente após conclusão confirmada.
- Criado workflow público \`.github/workflows/publish-qa-release.yml\` para criar **prerelease QA imutável** no GitHub contendo APK, checksums, relatório de assinatura e ZIP do fonte editável. Ele baixa EXATAMENTE o artefato aprovado do CI 38815466466 e só publica depois de verificar commit/conclusão/hashes. Primeira execução 38821056294 falhou porque SHA256SUMS.txt usava prefixo \`delivery/\`; corrigido na commit \`b2e6789fcbf5116e916ef6837fdaed368ee56c01\`; nova execução ainda não confirmada como sucesso.
- Segurança e limites sem mudança: assinatura permanente de release, assinatura independente do manifesto remoto, QA Wi-Fi/Samsung/PvP real e IA multiusuário ainda não concluídos. Consultar issue #38 e \`docs/FEATURE_AUDIT_0.5.30.md\`. Não alterar branch estável por esse QA.


### 2026-10-15 — Tentativa de prerelease permanente GitHub: permissão da integração bloqueada

- O workflow de prerelease público verificou com SUCESSO os checksums SHA-256 do APK e do ZIP de fontes do run aprovado 38815466466.
- A publicação do GitHub Release falhou na etapa gh release create com **HTTP 403 Resource not accessible by integration**, run 38821135771. O token efêmero de Actions tinha contents:write indicado nos logs, mas o endpoint de Releases recusou a integração.
- O workflow foi ajustado para **execução manual** e exige Secret de repositório PVPPOKEGO_RELEASE_TOKEN com permissão de publicação de releases; não inclui token embutido, nunca colocar chave no código. Commit público ed5f5c3a272e9f7a6dd258315a9508db0c60c2ff.
- **GitHub Release NÃO foi criado.** O GitHub contém código completo da branch de compilação e artefatos de Actions com retenção limitada. As cópias confirmadas no Google Drive permanecem a distribuição dos pacotes (APK QA, projeto editável e pacote com acervo histórico).
- O issue #38 foi atualizado com bloqueio de release e observação da captura do emulador. Nova validação UiAutomator foi adicionada à CI, mas NÃO declarar aprovada enquanto estiver em execução.

### 2026-10-15 — Pacote editável ampliado e validação UiAutomator (checkpoint)

- Distribuição já confirmada do APK QA 0.5.30 e ZIP editável public build no Google Drive: IDs 1cDZ_GHE_vMpjieFl2Al38wdUWYfmncz- e 1LTrMj6DaUTOiJgQr7TYqHQE71VGNfOln.
- Distribuição do pacote complementar editável com histórico privado separado (snapshot 06/10) confirmada no Drive: https://drive.google.com/file/d/1TNVfXolp6YnFw5tjV9VE5gUDVcpa8hmk/view?usp=drivesdk.
- Fonte histórica ORIGINAL_ICONS versão 0.5.10, fornecida originalmente nesta conversa e preservada, copiada para a mesma pasta Drive como arquivo separado: https://drive.google.com/file/d/1_RGnnsnZXLJ1pJwUXswxFcUKYd0D3RMS/view?usp=drivesdk. Trata-se de um ZIP histórico de código/ícones, NÃO substituir automaticamente os ícones do build 0.5.30.
- Pacote local alternativo NOVO inclui o snapshot Android buildável atual na raiz, o acervo privado datado 06/10/2026 sob diretório isolado e material histórico 0.5.10 sob outra pasta isolada, mais manifesto de proveniência/links; nome: PvPPokeGo-0.5.30-FONTE-EDITAVEL-E-ACERVO-COMPLETO.zip, 7.300.714 bytes, SHA-256 e12272639376117ca9fb430b6434dd17e71bd422bd6be72dd29c885290db933b; 346 arquivos testados com zipfile.testzip() PASS. Esse pacote está no ambiente local da sessão, NÃO afirmar upload ao Drive até verificar.
- Inspeção da captura do emulador 0.5.30 encontrou modal Pixel Launcher ANR no screenshot apesar do job antigo verde. Novo job UiAutomator run 38820881551 confirmou que o aplicativo continha 'PvPPokeGo' e 'Assistente visual' na hierarquia, mas falhou no script shell pois emulator-runner executa as linhas individualmente; não foi detectada falha Kotlin/Java nesse run. O erro de sintaxe foi corrigido usando condicionais numa única linha, commit público 8ee99c3a7e41e4ac10b1b76a3a2f1eeea131c09d. Aguardar novo job strict UI antes de marcar smoke visual aprovado.
- GitHub prerelease permanece BLOQUEADO por erro de permissão HTTP 403 no token de Actions. Workflow de prerelease modificado para disparo MANUAL com token guardado em GitHub Actions Secrets (nome PVPPOKEGO_RELEASE_TOKEN), commit público ed5f5c3a272e9f7a6dd258315a9508db0c60c2ff. NÃO incluir token em texto/ZIP e NÃO afirmar que a release permanente foi publicada.
- Ainda pendentes físico Samsung + Pokémon GO, assinatura de manifesto com chave própria, keystore permanente de APK release, rede Wi-Fi real, backend de IA com consentimento e artes finais aprovadas. Issue #38 foi atualizado. Preservar branches estáveis.

### 2026-10-15 — Arquivo editável ampliado no Google Drive (verificado)

- Arquivo PvPPokeGo-0.5.30-PROJETO-EDITAVEL-COMPLETO-ACERVO.zip, tamanho exato 7.300.714 bytes, SHA-256 e12272639376117ca9fb430b6434dd17e71bd422bd6be72dd29c885290db933b, integridade ZIP testada.
- Drive ID 1UipiTkfV2TKSbUO2qvjHmYcFUGkqQlIz, URL https://drive.google.com/file/d/1UipiTkfV2TKSbUO2qvjHmYcFUGkqQlIz/view?usp=drivesdk.
- Upload realizado; metadata lida de volta: nome e tamanho correspondem, pasta PvPPokeGo ID 1ybogVRRnaOe0KtWjlovxS56bJOi_JewI correta.
- Conteúdo: fonte Android 0.5.30 QA compilável e editável na RAIZ, acervo privado de 06/10/2026 isolado por pasta, referência 0.5.10 ORIGINAL_ICONS de 28/09/2026 separada, inventário com origem e hashes. Histórico antigo não foi sobreposto ao código mais novo.

### 2026-10-15 — Inspeção de interface acessível do Android 35 (recuperação de artefato)

- Do run público 38820881551, artefato UiAutomator XML ID 12012906748, obtido e analisado por XML parser offline.
- A árvore raiz e os 17 nós com textos eram do pacote com.lucianotoscano.pvppokego, incluindo PvPPokeGo, Assistente visual de batalhas PvP, Iniciar, Parar, Gravar, Nova batalha, Reconhecimento automático, Modo Batalha Assistido e Versão 0.5.30.
- NENHUM nó da árvore continha Pixel Launcher ou alerta de ANR. Isto demonstra que a UI inicial estava presente na hierarquia de acessibilidade desse run e melhora substancialmente a evidência anterior.
- Mesmo assim, job CI 38820881551 resultou failure porque o action emulator-runner executou um comando if ... then multilinha como comandos shell separados, erro de sintaxe 'expecting fi'. Erro do script corrigido no commit público 8ee99c3a7e41e4ac10b1b76a3a2f1eeea131c09d; próximo run rígido 38821812163 não confirmado no momento da atualização.
- Confirmação de tela inicial por UI XML não prova funcionamento de HUD/OCR em batalha; QA físico continua aberto no issue #38.

### 2026-10-15 — Pacote geral local de 175,6 MB (substitui a entrega incompleta de 7 MB)

- O antigo arquivo `PvPPokeGo-0.5.30-PROJETO-EDITAVEL-COMPLETO.zip` de 7.300.714 bytes possuía 346 registros e ~14,9 MB descompactados, mas **não incluía nenhum APK**. Não descrevê-lo como distribuição binária integral.
- Novo ZIP consolidado da sessão: `PvPPokeGo-0.5.30-TUDO-PROJETO-APK-CODIGO-HISTORICO.zip`, 175.617.919 bytes, 479 registros, 186.981.686 bytes descompactados; `unzip/zipfile testzip` passou; SHA-256 `425c006c0f68b7bb491ab29be659d3aae0471a71def5f7cc2d6749b6f666474c`.
- Inclui projeto Android 0.5.30 editável com código e Gradle Wrapper, APK real 0.5.30 QA (~80 MB), APK 0.5.29 QA (~80 MB), projeto 0.5.29, ZIP bruto dos ícones 0.5.10, snapshot privado histórico de 2026-10-06, resultados de assinatura e checksum, capturas de emulador, README e inventário com SHA-256 por item.
- O conteúdo do ZIP atual **não é um clone integral do histórico Git da branch privada**. O snapshot privado incluído é antigo e está isolado; histórico/HEAD canônico precisa ser obtido da conta GitHub com acesso autorizado. Não há keystore, tokens nem dados pessoais de terceiros no pacote.
- Foi tentado upload do ZIP único no Google Drive via conector. Falhou com HTTP 413 porque 175.617.919 bytes excedem o limite de download do conector de 104.857.600 bytes. Isso **não é prova de upload bem-sucedido**.
- Para distribuição Drive, o mesmo conteúdo foi separado em **dois ZIPs autônomos** extraíveis na mesma pasta: `PvPPokeGo-0.5.30-PARTE-1-APK-E-CODIGO.zip` de 87.717.386 bytes, SHA-256 `8aa91cb6a580c5107314e46462a3792fde372692ba78fda400fd8d96a82e4e33`; `PvPPokeGo-0.5.30-PARTE-2-BACKUPS-HISTORICOS.zip` de 87.882.092 bytes, SHA-256 `654e8b388a1323df7c067a9764992f8c82bdcebcabaad575a3a776bbe2ec6f2a`. Ambos passaram testzip. **Somente marcar Drive publicado se confirmação explícita do conector existir.**
- O pacote único de 175,6 MB foi criado e validado na sessão, disponível via sandbox no chat correspondente. O download da distribuição anterior QA + fonte pelo Drive continua existente e verificado.

## 12. Regra final

Sempre que o usuário pedir:
- “me manda o texto para novo chat”;
- “continue em outro chat”;
- “não quero perder o contexto”;

a resposta deve incluir obrigatoriamente o link para este arquivo e instruir o novo chat a lê-lo primeiro.

**Este documento deve crescer junto com o projeto. Não é descartável e não deve ser substituído silenciosamente por um resumo novo.**
