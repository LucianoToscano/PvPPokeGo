# ARQUIVO HISTÓRICO — Handoff 0.5.20

> Conteúdo arquivado para preservar contexto histórico. Não usar como estado atual do projeto.
> Estado atual: consulte `docs/PROJECT_CONTINUITY_GUIDE.md`.

# PvPPokeGo — Handoff atual 0.5.20

Atualizado em: 2026-10-06  
Branch estável: `v04-v03-stable-base`  
Package: `com.lucianotoscano.pvppokego`  
Versão: `0.5.20` / versionCode `25`

## Release validada

- PR #22: **PvPPokeGo 0.5.20 — continuidade, identidade visual por forma e modo gravador externo**
- merge commit: `475a6e68f9830f4d79019429e456f3a965d56bc8`
- release branch validada: `release/0.5.20`
- build run: `37492085202`
- artifact ID: `11425824095`
- APK: `PvPPokeGo-0.5.20.apk`
- SHA-256: `c134b25fc817384d3b6933eb95e77c15b9b2a36205c9959930829047ea9a1772`
- resultado: `testDebugUnitTest + assembleDebug + unzip -t` = sucesso

## O que a 0.5.20 resolveu

### 1. Correspondência e continuidade de batalha

A sessão não deve mais ser dividida por troca, animação de carregado, um frame ruim de OCR ou PC truncado.

Componentes:
- `BattleContinuityPolicy`
- `BattleCorrespondenceTracker`
- `BattleHistorySegmentMatcher`

Regras importantes:
- troca de apenas um lado permanece na mesma partida;
- uma leitura incompatível isolada é segurada;
- novo adversário desconhecido precisa de confirmação;
- Pokémon conhecido que retorna é aceito imediatamente;
- PC divergente sozinho nunca cria nova partida;
- tela de equipe completa e confiável pode encerrar/iniciar fronteira;
- formas regionais/alternativas com mesmo nome são identidades distintas pelo `speciesId`;
- rematch rápido normal não é unido apenas por identidade;
- fragmentos realmente iniciados no meio da luta podem ser reconectados no histórico.

### 2. Reconhecimento de Pokémon por imagem + OCR + PC

Foi adicionado um terceiro sinal de identidade: a imagem do próprio Pokémon.

Arquivos:
- `detect/PokemonVisualRecognizer.kt`
- `tools/package_visual_fingerprints.py`
- `assets/pokemon_visual_fingerprints.json`

O banco visual:
- é offline;
- contém fingerprints compactos, não um catálogo inteiro de imagens;
- é gerado a partir de renders HOME;
- preserva `speciesId`/forma;
- cobre centenas de entradas;
- usa forma exata quando o banco permite;
- quando uma imagem é ambígua entre formas do mesmo Dex, não inventa identidade: exige OCR/PC/outro sinal.

Fusão de identidade:
1. PC confiável do card;
2. espécie/forma visual confiável;
3. OCR de nome;
4. eliminação apenas quando segura.

Depois da identidade confirmada, tipos, golpes e matchup vêm do banco PvPoke.

### 3. Formas e estado exato por Pokémon

O estado interno passou a preservar `speciesId`/forma exata em:
- equipe;
- Pokémon ativo;
- reservas;
- trocas;
- movesets;
- tipos;
- energia;
- restauração de estado;
- correspondência de batalha.

Duas formas com mesmo nome/Dex podem manter energia separada.

### 4. Reservas e matchup

- `ReserveCardMatcher` usa PC + espécie visual exata + Dex apenas quando seguro.
- CP tem prioridade quando é único.
- Evidência visual exata pode recuperar card cujo CP ficou ilegível.
- forma visual exata vence um Dex compartilhado.
- Dex ambíguo sozinho não confirma forma.
- reserva não confirmada não recebe recomendação específica confiável.
- `identitySource` registra de onde veio a confirmação.
- tipos e matchup ficam escondidos/desconhecidos enquanto a identidade é insuficiente.

### 5. Carregados duplicados

Foi criado `ChargedAnimationCycleGate`.

O mesmo carregado visto em vários frames da mesma animação:
- gera um único evento;
- desconta energia uma única vez;
- não rearma por uma falha isolada de OCR.

Dois carregados legítimos consecutivos continuam possíveis depois que a animação anterior realmente terminou.

### 6. Dois carregados do inimigo

Foi criado/reforçado `ChargedMoveSlotResolver`.

Regras:
- primeiro carregado observado ocupa o primeiro slot disponível;
- segundo golpe **distinto** ocupa o segundo slot;
- repetir o mesmo golpe mantém o mesmo slot;
- escolha manual não é sobrescrita por observação automática;
- dois slots manuais não são substituídos automaticamente.

### 7. PC estável

`StableCpTracker` impede que leituras como 1486 → 143 ou 1499 → 149 substituam imediatamente o valor confiável.

- um frame truncado não vira verdade;
- uma leitura ruim isolada não substitui CP estável;
- mudança real de sujeito limpa o contexto antigo;
- o motor restaura CP pelo slot exato/forma antes de fallback por nome.

### 8. Ataques rápidos e energia

`FastMoveSignalFuser` e `BattleFrameEventDetector` foram endurecidos:
- queda de HP é o sinal principal;
- microqueda pode acumular;
- duração/cadência do Fast Move limita duplicação;
- quedas compatíveis com carregado são rejeitadas para contagem FAST;
- produção não cria Fast Move apenas por movimento visual;
- pode recuperar rápidos perdidos por cadência quando há evidência de HP;
- energia é mantida por Pokémon/forma e respeita cap;
- carregado observado desconta energia uma vez.

### 9. Recomendações e confiança

Recomendação específica exige contexto suficiente.

- identidade fraca/ambígua não deve gerar conselho específico como se fosse confirmado;
- visual de baixa confiança não pode substituir estado confiável;
- reserva não confirmada é excluída de recomendação;
- moveset observado/manual/estimado permanece distinguido;
- golpes estimados da reserva não devem ser apresentados como confirmação real.

### 10. Histórico e exportação

O histórico mantém:
- JSON;
- CSV;
- TXT legível;
- exportação por batalha ou completa;
- início/fim de sessão;
- motivo de encerramento;
- trocas estáveis;
- snapshots de estado;
- HP;
- energia/faixa;
- escudos;
- ativos;
- confiança;
- fonte dos eventos;
- versão real do app;
- versão/snapshot da base PvPoke;
- resultado apenas quando detectado.

A UI do histórico separa por batalha e por grupos:
- Resumo;
- Você;
- Inimigo;
- Assistente;
- Diagnóstico técnico.

### 11. Gravação de tela

Existem dois caminhos:

**Gravação interna**
- `ScreenFrameRecorder`;
- MediaCodec/MP4;
- validação de arquivo;
- erro do encoder é mostrado em vez de falhar silenciosamente.

**Gravador Samsung/externo**
- novo `PvPPokeGoAccessibilityCaptureService`;
- usa a API de screenshot do AccessibilityService como fonte alternativa;
- não lê nós da interface e não executa toques;
- opção de iniciar overlay em modo compatível sem abrir MediaProjection;
- se outra gravação revoga MediaProjection, o serviço não encerra automaticamente e tenta migrar para a captura de acessibilidade.

Limitação real:
- a API de screenshot de acessibilidade tem taxa menor que MediaProjection;
- OCR/identidade/tipos/matchup continuam úteis;
- contagem FAST de alta frequência pode ficar menos precisa;
- compatibilidade Samsung/gravadores externos ainda precisa de validação em aparelho real com o serviço de acessibilidade habilitado.

### 12. Ícone oficial

O ícone oficial é a imagem enviada pelo usuário: **Icon PVPPokeGo.png**.

No projeto:
- `app/src/main/res/drawable-nodpi/pvppokego_launcher.png`
- `drawable/pvppokego_launcher_foreground.xml`
- `mipmap-anydpi-v26/ic_launcher.xml`
- `mipmap-anydpi-v26/ic_launcher_round.xml`
- Manifest usa `@mipmap/ic_launcher` e `@mipmap/ic_launcher_round`.

Não substituir por Android genérico.

## Testes unitários importantes

Arquivos atuais:
- `BattleHistorySegmentMatcherTest`
- `PokemonTypeSanitizerTest`
- `BattlePresenceTrackerTest`
- `FastMoveSignalFuserTest`
- `BattleContinuityPolicyTest`
- `BattleCorrespondenceTrackerTest`
- `BattleStateStoreTest`
- `ChargedAnimationCycleGateTest`
- `ChargedMoveSlotResolverTest`
- `EnergyTrackerTest`
- `ReserveCardMatcherTest`
- `StableCpTrackerTest`

Casos cobertos incluem:
- animação de carregado repetida em vários frames = 1 evento;
- carregados consecutivos legítimos;
- segundo carregado distinto ocupa segundo slot;
- escolha manual preservada;
- CP truncado não substitui CP confiável;
- troca não divide batalha;
- um frame ruim não cria nova batalha;
- rematch normal permanece separado;
- fragmento de batalha pode ser reconectado;
- formas regionais distintas;
- energia separada por forma;
- Cramorant/Rillaboom por CP;
- recuperação visual quando CP não é legível;
- Dex compartilhado permanece incerto sem sinal adicional;
- Rillaboom somente Planta;
- Cramorant Voador/Água.

## Arquivos-chave alterados na 0.5.20

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `capture/AccessibilityScreenCapture.kt`
- `capture/ScreenCapture.kt`
- `capture/ScreenFrameRecorder.kt`
- `data/Models.kt`
- `data/GameDataRepository.kt`
- `data/BattleHistoryRepository.kt`
- `data/BattleHistorySegmentMatcher.kt`
- `detect/BattleOcrDetector.kt`
- `detect/PokemonVisualRecognizer.kt`
- `detect/BattleFrameEventDetector.kt`
- `engine/BattleEngine.kt`
- `engine/BattleAssistAdvisor.kt`
- `engine/BattleContinuityPolicy.kt`
- `engine/BattleCorrespondenceTracker.kt`
- `engine/BattleStateStore.kt`
- `engine/ReserveCardMatcher.kt`
- `engine/ChargedAnimationCycleGate.kt`
- `engine/ChargedMoveSlotResolver.kt`
- `engine/StableCpTracker.kt`
- `overlay/BattleOverlayService.kt`
- `overlay/BattleOverlayView.kt`
- `overlay/QuickOverlayMenu.kt`
- `ui/SettingsScreen.kt`
- launcher/adaptive icon resources
- `tools/package_visual_fingerprints.py`
- `.github/workflows/release-0.5.20.yml`
- `.github/workflows/package-project-zip.yml`
- testes correspondentes.

## O que ainda depende de aparelho/vídeo real

1. Samsung Screen Recorder + modo de acessibilidade habilitado.
2. Gravadores externos específicos.
3. Precisão FAST em captura de acessibilidade mais lenta.
4. Pokémon/formas/costumes raros que não tenham fingerprint visual forte.
5. Ajustes finos de regiões visuais em outras proporções/resoluções de tela.
6. Comportamento do encoder MP4 em modelos Android diferentes.

Esses itens não invalidam o build; são validações de hardware/cenário real.

## Próxima versão

A próxima versão nova deve ser **0.5.21**.

Nunca gerar outro binário diferente chamado 0.5.20.
