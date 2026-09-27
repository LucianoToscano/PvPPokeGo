# PvPPokeGo — Handoff atual

Atualizado em: 2026-09-27
Branch: `v04-v03-stable-base`

## LEIA PRIMEIRO

O contexto mestre atual está em:

`docs/MASTER_PROJECT_CONTEXT_2026-09-27.md`

Qualquer novo chat, conta, GPT ou desenvolvedor deve ler esse arquivo antes de continuar o projeto.

Para histórico anterior mais extenso, a branch `main` também contém:

- `docs/CONSOLIDATED_PROJECT_CONTEXT_2026-09-26.md`
- `docs/VISUAL_CONTRACT_2026-09-26.md`

## Estado técnico atual

Package: `com.lucianotoscano.pvppokego`

Versão atual no código:

- `versionCode = 6`
- `versionName = 0.5.1-overlay-controls-fix`

Android atual:

- minSdk 29
- compileSdk 36
- targetSdk 36
- Java 17
- Gradle CI 8.11.1

Build principal: `.github/workflows/android-debug.yml`

Último build auditado e validado:

- run `#89`
- run id `36317141354`
- conclusão: `success`
- head `54a413dd4ffe20476e66fab1a1161f42e3298e2f`
- artifact id `10931047917`
- artifact `PVPPokeGo-V05-FIX2-audited-build`
- APK interno: `PVPPokeGo_V05_FIX2-audited-debug.apk`
- SHA-256 do APK: `909a9956539e5051aa1e9ae7df1e51ba4580ac0e5bd59c53b7dbabb653a98e82`
- validação: `lintDebug + testDebugUnitTest + assembleDebug`
- lint: 0 erros bloqueantes; 70 avisos não bloqueantes

## Regras que não podem regredir

O PvPPokeGo continua sendo assistente externo: MediaProjection + visão/OCR + overlay. Não injeta código, não modifica Pokémon GO e não automatiza cliques/gestos no jogo.

O HUD não pode cobrir nome/PC, HP, Pokébolas, escudos, tipos nativos, botões de ataque do jogador nem áreas importantes de toque.

Energia inimiga e energia própria são modelos separados. Fast Move inimigo só deve ser contado por evidência do inimigo; ação do jogador não pode incrementar energia inimiga.

Cada Pokémon adversário preserva energia/contagem ao trocar e voltar.

## Funções V05 integradas

- confiança de presença de batalha
- HUD oculto fora da batalha automática
- modo manual disponível mesmo sem `battleActive`
- energia inimiga com estado confirmado/estimado/faixa
- energia própria para recomendações
- Battle Assist / Copilot
- Shield Advisor
- reservas `MELHOR / NEUTRO / EVITAR`
- histórico opcional do inimigo
- topo ancorado dinamicamente aos cards nativos
- ataques inimigos com visual de Charged do GO
- quick menu básico/avançado
- menu ícone+texto ou somente ícones
- engrenagem independente e arrastável
- badge de liga independente e arrastável
- detecção de Liga Grande/Ultra/Mestra/Auto
- olho para ocultar/restaurar HUD
- cadeado global para travar/destravar posições
- modo edição avançado com escala/opacidade/hide/undo/reset

## Auditoria V05 FIX2 — bugs corrigidos

Além do reparo original de arraste `66e40a9247bee74b489dd724e630ac7f5014a18e`, a auditoria encontrou e corrigiu:

1. HUD desaparecendo no modo manual quando reconhecimento automático estava desligado.
2. Submenu de golpes recusando toque no modo manual por exigir `battleActive=true`.
3. Locks/hide individuais não respeitados em alguns hit-tests de tipos fortes, ataques e reservas.
4. Reset de layout incompleto, que preservava escala/opacidade/lock/edit global.
5. Estado antigo de edição/free-drag podendo reviver após sessão interrompida.
6. Corrida de ciclo de vida ao iniciar MediaProjection, capaz de deixar HUD preso oculto ou aparecer sobre a própria tela do app.
7. Corrida de memória no `ScreenCapture`: bitmap podia ser reciclado enquanto outra thread copiava o frame.
8. OCR filtrado por cor bloqueando indevidamente o fallback para OCR da imagem original quando gerava texto inválido.
9. Uso de `currentWindowMetrics` sem fallback de API, incompatível com Android 10 apesar de `minSdk=29`.
10. Ausência de testes Android reais no módulo `app`; foram adicionados testes de energia residual/faixa/cap e presença/histerese de batalha.
11. Modo edição explicitamente escolhido antes de um início limpo agora é preservado; somente sessão anterior interrompida limpa o estado por segurança.

## Controles atuais da overlay

- olho: ocultar/mostrar HUD inteiro; quando oculto, somente o olho deve permanecer para restaurar;
- cadeado: destravar/travar posições;
- engrenagem: abrir menu rápido; arrastável quando cadeado está aberto;
- badge da liga: mostrar modo atual; arrastável independentemente quando cadeado está aberto.

Esses controles devem ser independentes, não colados uns aos outros.

## Limitação conhecida de interação

Enquanto o cadeado global está aberto ou o modo edição está ativo, a implementação atual usa uma camada transparente fullscreen para selecionar/arrastar blocos. Portanto, essa fase de posicionamento pode interceptar os toques do Pokémon GO.

Regra de uso atual: posicionar o HUD e **fechar o cadeado antes de batalhar**. O serviço sempre rearma o cadeado fechado ao criar uma nova sessão de overlay.

Uma evolução futura pode substituir essa camada fullscreen por touch-zones de arraste por bloco para permitir edição sem capturar o restante da tela.

## Dados PvP

PvPoke continua como fonte principal para rankings, movesets, energia e matchups.

RankedBoost pode ser usado apenas como fonte secundária de stats gerais/consulta, não como ranking PvP principal.

## Próxima validação em aparelho

Testar a FIX2 auditada nesta ordem:

1. iniciar overlay e entrar em batalha;
2. tocar no cadeado e confirmar mudança visual fechado/aberto;
3. com cadeado aberto, arrastar ataques, reservas, ícones do topo, Assistido, olho/cadeado/engrenagem/badge;
4. fechar cadeado e confirmar que os elementos ficam fixos;
5. tocar no olho e confirmar que tudo desaparece exceto o próprio olho;
6. restaurar pelo olho;
7. testar modo manual com Reconhecimento automático desligado;
8. testar submenu dos ataques em modo manual;
9. confirmar reconhecimento do jogador/oponente e contagem durante uma batalha real;
10. em Android 10, confirmar que a captura inicia sem crash.

Se houver falha em aparelho, usar esta FIX2 como baseline e corrigir incrementalmente; não voltar ao APK V05 de SHA `5a04510515e89d0acd0473ae5ed1ec4b52f746cde0e0410134a1b637afedff8a`.