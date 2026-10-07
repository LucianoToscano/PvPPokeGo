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

## 12. Regra final

Sempre que o usuário pedir:
- “me manda o texto para novo chat”;
- “continue em outro chat”;
- “não quero perder o contexto”;

a resposta deve incluir obrigatoriamente o link para este arquivo e instruir o novo chat a lê-lo primeiro.

**Este documento deve crescer junto com o projeto. Não é descartável e não deve ser substituído silenciosamente por um resumo novo.**
