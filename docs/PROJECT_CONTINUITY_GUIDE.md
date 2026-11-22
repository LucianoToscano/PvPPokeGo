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


### Continuidade oficial após QA 0.5.26 — avançou para release/0.5.27

A partir do relato visual do usuário em 2026-10-07 (cards das reservas poluídos, ícones ocultos durante troca, launcher diferente), o desenvolvimento ativo está em **`release/0.5.27`**. Ler primeiro o novo [Guia Mestre 0.5.27](https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.27/docs/PROJECT_CONTINUITY_GUIDE.md) e o [Checklist 0.5.27](https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.27/docs/CHECKLIST-PvPPokeGo-0.5.27.md). O histórico desta branch 0.5.26 permanece preservado, sem reset ou mudanças no código.


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



## 22/11/2026 — Complemento transversal: PGSharp PvP, engenharia reversa, Sniper e referências PGBR LT034/LT035

**Status:** `REGISTRADO` — referência técnica e visual; **nenhum recurso PvPPokeGo foi implementado, corrigido ou homologado por este complemento**, nenhum APK PvPPokeGo foi gerado. Base deste registro: `release/0.5.26` @ `4852e1307cd0aedc1602e3fed6d6fd47dee92fbb` antes da mudança documental; preservar o conteúdo histórico, as melhorias posteriores de HUD documentadas e a separação dos elementos de cada aplicativo.

### Evidência visual exata e proveniência
- [Pasta PGSharp](https://drive.google.com/drive/folders/1WAEmEpZmvUw9dDiapNcygtmZQq6XIB_j), [dossiê da auditoria de 16 imagens e 20 vídeos](https://drive.google.com/file/d/11_OSo_6hPaXeRB5GW-HLXbVSr5HyugyO/view) e [inventário CSV com IDs](https://drive.google.com/file/d/1pvXnzt41spOD82q1u1_i5ta4Xnm0a2SO/view). As imagens foram inspecionadas, e os vídeos examinados por **amostras de quadros**, sem comprovação de mecanismo de controle.
- Vídeos **13 e 14** do PGSharp mostram barra flutuante com **três botões independentes: “Ataques ágeis”, “Ataques carregados”, “Shield”**; há batalha, golpear, escudar e trocar na gravação. **São elementos do PGSharp, não controles nativos Pokémon GO e não são ícones/funções próprios do PvPPokeGo**. Essa distinção já consta no inventário deste Guia (seção do HUD). A imagem/vídeo **não prova** que o sistema acionou o botão, escolheu automaticamente o segundo ataque supereficaz ou tomou decisão de escudo.
- Vídeo 12: Quick Sniper I e II configuráveis vertical/horizontal e independentes; vídeo 11: Radar Próximos separado. Eles são **fora do escopo PvPPokeGo**, salvo referência visual de overlays compactos; não integrar feed/teleporte a este projeto por efeito colateral.
- [Auditoria de sprites/joystick/efeitos](https://drive.google.com/file/d/1PjXJr87jK2F801_lz7A7x9OK0_Bhmyy9/view) e [menus XML/código/licenças](https://drive.google.com/file/d/1aKp7Is0q6yFenLdBsiG2D582T6D_ha5u/view): no plugin 1.278.0 há 1.973 PNG e menus Android vetoriais; os ícones de PGSharp não estão todos em PNG. Materiais do jogo/cliente de terceiros não podem ser tratados como recursos próprios ou licenciados apenas por estarem no acervo.

### Cadeia de código investigada e limites
O estudo histórico de [Romain Thomas](https://github.com/romainthomas/pgsharp) trata da VM Lua, ponte JNI/Unity/IL2CPP do PGSharp 1.33.0. No plugin 1.278.0 aparecem classes nativas `PL.NRL/NSMTC/PUL` e strings `pvpCombatStats`, `battleAssistant`, `rocket.shield`, sem demonstração de cadeia funcional da automação PvP nem equivalência de offsets antigos/novos. **Strings, modos visíveis e DEX extraído não bastam para reproduzir ou homologar autoescudo/ataque carregado.** Investigar, em teste autorizado, o caminho do evento e distinguir ações de PvP, Rocket, Ginásio e Reide. Não confundir troca de configuração compartilhada na UI com prova de que o backend implementa os quatro modos.

### Aplicação no desenvolvimento PvPPokeGo (ainda pendente)
- Preservar filosofia própria: OCR/observação da tela, confiança explícita e HUD de aconselhamento, sem tocar no jogo no modo assistido. Implementações automáticas devem ter autorização, escopo e teste separados; não converter a existência dos botões PGSharp em executor PvPPokeGo.
- O requisito de priorizar **golpe carregado supereficaz antes do primeiro golpe já carregado** continua candidato, não implementado por esta nota. Modelar custo de energia, efetividade por tipo duplo, disponibilidade de dois golpes, shields próprios/inimigos, fase de turno, cooldown de troca, risco de timing e decisão reversível; deduzir de eventos confirmados, não apenas animações.
- Validar **todas as dimensões de batalha**: ágil, carregado, pergunta/uso de escudo, câmera, texto “X usou Y”, dano/buffs, HP, energia e troca forçada, evitando perda dupla de energia/ações. Testar UI com PGSharp presente e sem PGSharp, sem conflito ou sobreposição de botões, e em aparelho Samsung físico. Conservar matrizes de estado e checklists já descritos neste Mestre.
- A [PGBR PR #72](https://github.com/LucianoToscano/PGBR/pull/72) introduziu apenas um Sniper 100% IV do feed próprio do PGBR; [PR #73](https://github.com/LucianoToscano/PGBR/pull/73) um painel PGSharp Free/Standard e key local ainda não autenticada. [LT034 QA](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.34-LT) publicou APKs; [LT035 QA](https://github.com/LucianoToscano/PGBR/releases/tag/v0.00.35-LT) adicionou observação/ciclo de vida PvP na linha PGBR. **Nenhuma dessas publicações atualiza automaticamente o PvPPokeGo** nem prova ataques rápidos/carregados, shields ou priorização de supereficaz em GBL. `device_tested=false` para os testes físicos referidos.
- O proprietário já declarou autorização relevante para APKs/APIs no Guia Mestre PokeSearch LT364/LT365; não repetir a mesma solicitação. Em qualquer integração efetiva, verificar fronteiras reais do serviço, dados/protocolos e direitos dos assets. [PGRazor](https://github.com/jpitty03/PGRazor) oferece código MIT, mas não licença geral para toda arte de terceiros.

**Próxima etapa:** se houver trabalho de implementação PvP, abrir branch própria a partir da HEAD correta e anexar provas de compilação, vídeo de teste, versão, espécie/moves, resultado do escudo e logs anonimizados. Este adendo preserva o histórico, não altera Maven/Gradle, dataset, serviços, APKs nem menus.


## 12. Regra final

Sempre que o usuário pedir:
- “me manda o texto para novo chat”;
- “continue em outro chat”;
- “não quero perder o contexto”;

a resposta deve incluir obrigatoriamente o link para este arquivo e instruir o novo chat a lê-lo primeiro.

**Este documento deve crescer junto com o projeto. Não é descartável e não deve ser substituído silenciosamente por um resumo novo.**


<!-- REGISTRO PGSHARP 2026-11-22 — auditoria multimídia, engenharia reversa e integração -->
## Registro técnico transversal — PGSharp: binários, rastreamentos, vídeos e decisões (22/11/2026)

**Tipo:** registro cumulativo de pesquisa e referência visual, **não** comprovação de build, API autorizada, APK funcional, teleporte operacional ou PvP automatizado. **Origem:** investigação de chat com arquivos locais, material histórico de Romain Thomas e pasta PGSharp do Drive. O inventário de mídia foi lido do Drive e revisado. Todas as afirmações de endereços e fluxos binários exigem reprodução ao integrar em outra versão.

### Fontes e proveniência
- [Pasta PGSharp](https://drive.google.com/drive/folders/1WAEmEpZmvUw9dDiapNcygtmZQq6XIB_j); [auditoria visual de 36 arquivos](https://drive.google.com/file/d/11_OSo_6hPaXeRB5GW-HLXbVSr5HyugyO/view); [inventário CSV com IDs e descrições](https://drive.google.com/file/d/1pvXnzt41spOD82q1u1_i5ta4Xnm0a2SO/view); [dossiê ZIP](https://drive.google.com/file/d/12_EigP7_mA7pY7tXz4B4QsLiQ4pVyh3x/view).
- [Artigo de Romain Thomas sobre PGSharp (2021)](https://www.romainthomas.fr/post/21-11-pgsharp-analysis/); [apresentação Ekoparty](https://www.romainthomas.fr/publication/21-ekoparty-mobile-hacking-space-pgsharp/21-10-ekoparty-mobile-hacking-space-pgsharp.pdf); [repositório e `libmain.symbols`](https://github.com/romainthomas/pgsharp); [artigo Pokémon GO anti-Frida iOS](https://www.romainthomas.fr/post/21-07-pokemongo-anti-frida-jailbreak-bypass/) — o último é contexto histórico iOS, não mapa de funções Android atual.
- [Correlação histórica de símbolos e binários](https://drive.google.com/file/d/13gF5iC0aZgEabsnco3o72uIG-CYg4kmU/view); [extração textual ELF/JNI](https://drive.google.com/file/d/19fAxpqtCppc9gX-IX0eYcAg0oNDs-BCA/view); [mapeamento aprofundado v2](https://drive.google.com/file/d/1XZbdk3E3EWuZI1UwAd-9xbbpXnseznUQ/view).

### Auditoria visual — 16 imagens e 20 vídeos
As **16 imagens foram inspecionadas visualmente**; os **20 MP4 foram examinados por amostragem de quadros distribuídos na duração**, não exaustivamente quadro a quadro nem por instrumentação dinâmica. Os originais, caminhos e direitos de terceiros foram preservados. As gravações não identificam inequivocamente a versão PGSharp.
- **Quick Sniper I e II:** módulos e alternâncias independentes; orientação Vertical/Horizontal separada; o vídeo 12 mostra criação de filtros com espécies, IV, nível, gênero, forma, tamanho, distância, prioridade, notificações e barras de resultados coexistindo no mapa. Não confundir com o **Radar Próximos**, que tem filtros e notificações próprios (vídeo 11).
- **PvP:** vídeos 13 e 14 exibem três comandos de HUD distintos — **Ataques ágeis**, **Ataques carregados** e **Shield** —, golpes e trocas durante uma partida. A visualização **não prova execução automática**, decisão de escudo, seleção de ataque supereficaz ou disparo por hook nativo.
- **Movimento/localização:** favoritos, rotas/GPX, teleporte, velocidade, joystick flutuante com variantes claro/escuro e manopla arrastável, toque no mapa com pulso ciano, cooldown e histórico.
- **UI e privacidade:** menu flutuante e atalhos personalizáveis (ícones apenas ou ícone + texto; orientação; switches individuais); descarte de itens por regras; seleção de Pokémon com sprites/possíveis brilhantes; alternância de ocultação de nome apenas visual. Não alterar login/RPC/identidade real do treinador. A imagem promocional PGSharp_Config_LOT.png é cartaz ilustrado, não evidência de função, e seus QR codes não foram validados.

### Registro técnico reverso — versões e limites
- Estudo de 2021: PGSharp **1.33.0** com VM Lua customizada e bytecode `.luac` (`location.luac`, `pgodump.luac`, `pgo.luac`), ponte JNI, hooks IL2CPP/Unity. O arquivo `libmain.symbols` antigo nomeia `luaD_precall`, `init_lua_jni`, `hook_MapContentHandler__UpdateCells`, `hook_Map_PoiDirectoryService__AddPokemon/RemovePokemon` e `hook_Map_MapPokemon__TryCapture`. Os offsets de 2021 **não são reutilizáveis automaticamente em 2026**; símbolos `il2cpp_...` podem nomear *slots de ponteiro*, não instruções executáveis.
- PGSharp **1.278.0 / Pokémon GO 0.437.0**, biblioteca `libmain.so` ARM64 (SHA-256 `912b8a2d064b82eed1fe68c704a7dee2e89394846bc16dcb3e71c46a292d60e3`), `JNI_OnLoad=0x1C9734`, ELF parcialmente stripped. Inventários anteriores catalogaram 1.616 funções dinâmicas definidas na moderna e 1.595 na antiga, mais métodos `native` do jogo/SDKs; **não são contagens de recursos PGSharp**. Sem `mapping.txt` original ou reconstrução completa JNI → C validada.
- Extrações históricas deste chat reportaram APKs auxiliares embutidos nas `libmain.so` 1.33, 1.277.2 e 1.278.0, incluindo DEX da classe `PL`. Em 1.278 foram reportados XOR `0x35`, 3.777 entradas e classes `me.underw.hp.PL`; 1.277.2 XOR `0x23`, 3.757 entradas. Esses APKs **são componentes internos**, não instaladores completos, e tentativas no Android mostraram **“App não instalado”**. Assinatura e dependências de carregador faltantes precisam ser verificadas antes de qualquer uso.
- Referências DEX/strings reportadas na 1.278.0: `FASTSNIPE`, `FASTSNIPE2`, `hlfastsnipe*`, `pokemonFeed`, `FEEDS_BASIC/ADVANCED`, `SCANNEARBY`; `q4.d(...)` (preparação de consulta) e `q4.e(...)` (leitura `lat/lng`), `PL.NRL`, `PL.NSMTC`, `PL.PUL`, `HLVM`, `GL`. URLs literais reportadas em cliente: `f.pgsharp.com/peanuts` (Pokémon), `/walnut` (stops), `/macadamia` (quests), `/pistachio` (raids). **Nenhuma URL constitui API pública, contrato licenciado ou comprovação de acesso; nunca testar com credenciais de terceiros ou contornar autenticação.**
- Ponte histórica 1.33: ação 3 `plg.map.tp` via `PL.b(3, coords)` / `NRL`; o formato observado concatena componentes de `LatLng` com vírgula. **Não presumir comportamento ou assinatura equivalentes na moderna.** Hooks JNI de GPS 1.278 reportados na região `0x7ADE5C`, para `nativeAddLocationProviders`, `nativeLocationUpdate` e `nativeGpsStatusUpdate`; esses endereços exigem validação de endereço de execução, ABI e cliente-base em cada build.
- Correção de interpretação: `searchFilterPreferenceBase64` apareceu como preferência de dados de jogador, e `WildMapPokemon` como tipo do objeto no mapa; referências a strings **não demonstram** motor Quick Sniper, feed remoto ou endpoint funcional. Chaves `rocket.fast/charge/shield`, `pvpCombatStats` e `battleAssistant` **não provam** executor PvP automático.

### Procedimento de pesquisa e integração
Usar JADX/DEX e Ghidra para rastreio estático, ADB/Logcat/Perfetto e, apenas em ambiente autorizado, observação dinâmica Java/JNI/ARM64. Registrar versão, ABI, SHA, origem, timestamps, método tocado, entrada/saída e logs sem dados pessoais; separar Quick Sniper I/II, Radar Próximos, Rocket, PvP, ginásio/reide e Dynamax. Não usar keygen: não serve como rastreador de funções. Sem root, ADB é útil para logs; PC + Android de teste permite análise mais profunda; mesmo com root não há autorização implícita para APIs privadas.
**Prioridade:** (1) identificar callbacks de filtros e origem autorizada dos resultados; (2) confirmar seleção → `LatLng` → controle de localização; (3) validar independentemente HUD, golpes e escudos em testes controlados; (4) adaptar UI/joystick, privacidade e itens sem perder funcionalidades. Para assets, verificar licença e não importar sprites de capturas como se fossem PNGs transparentes originais.
**Resultado desta anotação:** documentação/referências; nenhum APK, feed ao vivo, batalha automática, assinatura ou autorização de backend foi criado ou homologado. Não há modificação no aplicativo comercial PGSharp, nem alteração da fonte oficial de outros projetos.
<!-- FIM REGISTRO PGSHARP 2026-11-22 -->

**Aplicação específica PvPPokeGo:** a barra PGSharp de três comandos é referência visual de UX; o projeto mantém OCR, rastreamento de energia, cálculo de matchups e HUD externos desacoplados de hooks/injeção. Nenhum vídeo demonstra seleção automática de ataque super eficaz ou shield inteligente; criar cenários de teste isolados, sem declarar execução real antes de QA físico.
