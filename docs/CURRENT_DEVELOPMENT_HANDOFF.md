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

Versão: `0.5.0-copilot-hardening`

Android atual:

- minSdk 29
- compileSdk 36
- targetSdk 36
- Java 17
- Gradle CI 8.11.1

Build principal: `.github/workflows/android-debug.yml`

Último build validado ao atualizar este handoff:

- run `#78`
- run id `36312971519`
- `success`
- head `4f48cda9238f1ea40d4223cbb7389541e7078cf1`

## Regras que não podem regredir

O PvPPokeGo continua sendo assistente externo: MediaProjection + visão/OCR + overlay. Não injeta código, não modifica Pokémon GO e não automatiza cliques/gestos no jogo.

O HUD não pode cobrir nome/PC, HP, Pokébolas, escudos, tipos nativos, botões de ataque do jogador nem áreas importantes de toque.

Energia inimiga e energia própria são modelos separados. Fast Move inimigo só deve ser contado por evidência do inimigo; ação do jogador não pode incrementar energia inimiga.

Cada Pokémon adversário preserva energia/contagem ao trocar e voltar.

## Funções V05 já integradas

- confiança de presença de batalha
- HUD oculto fora da batalha
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

## Correção mais recente — arraste travado

O usuário relatou que os ícones permaneciam travados mesmo após a introdução do cadeado global.

Reparo aplicado:

`66e40a9247bee74b489dd724e630ac7f5014a18e`

Mensagem do commit:

`fix: unlock all HUD blocks for free drag`

Mudança principal:

- `layoutLocked` passa a ser a fonte global de verdade;
- com cadeado aberto, locks individuais não impedem arraste;
- abrir o cadeado deve reconstruir a camada de drag imediatamente;
- fechar o cadeado remove a camada de drag e protege as posições.

O build `#78` passou. Ainda é necessário confirmar em aparelho que todos os blocos realmente arrastam.

## Controles atuais da overlay

- olho: ocultar/mostrar HUD inteiro
- cadeado: destravar/travar posições
- engrenagem: abrir menu rápido
- badge da liga: mostrar modo atual

Esses controles devem ser independentes, não colados uns aos outros.

## Dados PvP

PvPoke continua como fonte principal para rankings, movesets, energia e matchups.

RankedBoost pode ser usado apenas como fonte secundária de stats gerais/consulta, não como ranking PvP principal.

## Próxima ação recomendada

Primeiro validar o build mais recente em aparelho:

1. abrir cadeado;
2. arrastar ataques, reservas, ícones do topo, Assistido, olho/cadeado/engrenagem/badge;
3. fechar cadeado;
4. confirmar que tudo fica fixo;
5. testar olho ocultando absolutamente tudo e deixando somente o controle mínimo para restaurar.

Se algum bloco continuar imóvel, corrigir somente a camada de hit-test/drag daquele bloco sem redesenhar o projeto inteiro.
