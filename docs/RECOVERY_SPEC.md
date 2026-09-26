# PvPPokeGo — Especificação de reconstrução recuperada

Este documento é o ponto de referência para reconstruir o aplicativo após a perda do projeto Android original. Ele consolida o comportamento observado no APK antigo, os mockups visuais, o roadmap e o núcleo Kotlin já preservado no repositório.

## 1. Regra principal

PvPPokeGo é um **assistente visual externo**. A versão reconstruída deve:

- usar `MediaProjection` para observar a tela autorizada pelo usuário;
- usar `TYPE_APPLICATION_OVERLAY` para exibir informações;
- não modificar o APK do Pokémon GO;
- não injetar código;
- não acessar memória/processo do jogo;
- não interceptar rede do jogo;
- não usar root;
- não usar `AccessibilityService` para jogar;
- não enviar cliques, ataques, escudos ou trocas automaticamente.

Qualquer lógica chamada de “automation” no core deve ser tratada na camada Android como **recomendação visual**, nunca como execução de gestos.

## 2. Identificação da batalha

### Pokémon atual do jogador

Exibir/detectar:

- nome;
- CP/PC;
- tipo(s);
- indicador de matchup contra o inimigo atual:
  - `✓` forte;
  - `•` neutro;
  - `×` fraco.

### Pokémon inimigo atual

Exibir/detectar:

- nome;
- CP/PC;
- tipo(s);
- tipos que são fortes contra ele.

### OCR recuperado do APK antigo

Ponto de partida das ROIs normalizadas:

- faixa vertical dos cards: `y = 0.067 .. 0.15`;
- jogador: `x = 0.00 .. 0.25`;
- inimigo: `x = 0.75 .. 1.00`;
- cor-alvo de texto observada: `#2f5971`;
- tolerância observada: `30`.

OCR deve rodar em intervalo controlado e não a cada frame.

## 3. Reservas do jogador

Para os dois Pokémon que estão fora de campo:

- mostrar indicador `✓ / • / ×` contra o inimigo atual;
- mostrar tipo(s) abaixo de cada reserva;
- permitir seleção manual da equipe como fallback.

O nome das reservas normalmente não aparece no card lateral. Portanto OCR sozinho não é suficiente. A implementação confiável deve usar uma destas estratégias:

1. seleção manual da equipe antes da batalha;
2. template matching de sprite;
3. classificador local de sprite.

## 4. Triângulo de ataques do inimigo

Layout compacto:

- **Fast Move** no topo/centro;
- **Charged Move 1** embaixo à esquerda;
- **Charged Move 2** embaixo à direita.

Cada golpe usa ícone/círculo do tipo e nome compacto.

### Fast Move

Mostrar:

- ícone do tipo;
- nome, por exemplo `Ataque de Asa ▼`;
- contador de quantos Fast Moves o inimigo já usou desde a entrada/estado preservado daquele Pokémon.

O `▼` permite trocar manualmente o Fast Move entre os golpes possíveis daquele Pokémon quando a detecção estiver incerta.

### Charged Move 1 e 2

Para cada carregado mostrar:

- ícone do tipo;
- nome + `▼`;
- progresso circular da energia;
- parte ainda não carregada visualmente atenuada/cinza;
- número central/lateral = **quantos Fast Moves ainda faltam** para atingir o custo daquele carregado;
- quando pronto: número `0` e ícone totalmente ativo/colorido.

O `▼` permite escolher outro carregado possível do Pokémon.

## 5. Energia

O rastreamento por Pokémon inimigo deve:

- manter energia em `0..100`;
- somar o ganho real do Fast Move identificado;
- subtrair somente o custo real do Charged Move usado;
- preservar energia residual;
- suportar overfarm e carregados consecutivos;
- preservar o estado quando o adversário troca;
- restaurar exatamente o estado anterior quando aquele Pokémon volta;
- zerar/encerrar somente ao desmaiar ou ao reiniciar a batalha.

O núcleo `BattleEnergyTracker` no repositório já implementa essa regra e deve continuar sendo a fonte única de verdade.

## 6. Detecção de Fast Move

Nunca contar frames como ataques.

O detector deve usar ROI pequena e eventos visuais/temporais, considerando:

- duração/cadência dos Fast Moves candidatos;
- lag;
- animação de Charged Move;
- escudo;
- troca;
- entrada/saída de Pokémon;
- pausas/estados intermediários.

Requisitos:

- score/confiança;
- correção manual `+1/-1` ou equivalente;
- validar com gravações de tela antes de habilitar detecção automática por padrão.

## 7. Charged Move usado

Quando um carregado for revelado/usado:

- confirmar o golpe quando possível;
- registrar no histórico;
- descontar o custo de energia correto;
- manter energia residual;
- recalcular imediatamente os dois indicadores de carregados.

Se a identificação estiver incerta, oferecer fallback manual.

## 8. Trocas

Manter estado independente para os três Pokémon inimigos.

Exibir:

- timer da troca do jogador;
- timer da troca do adversário;
- progresso visual circular;
- tempo restante.

Ao detectar uma troca inimiga:

- não apagar energia nem contagem do Pokémon que saiu;
- vincular novo estado ao slot correto;
- restaurar o estado salvo se um Pokémon conhecido retornar.

## 9. Histórico rápido do oponente

Painel compacto/opcional contendo eventos recentes, por exemplo:

- `Ataque de Asa ×6`;
- `Nitrocarga — usou há 12 s`;
- `Trocou de Pokémon — há 42 s`.

O histórico deve ser temporário por batalha.

## 10. Dados PvPoke

PvPoke é fonte de dados **pré-processada/local**, não uma dependência online durante a luta.

Usar dados locais para:

- espécies/formas;
- tipos;
- Fast Moves;
- Charged Moves;
- ganho de energia do Fast Move;
- custo de energia do Charged Move;
- movesets e rankings de Grande/Ultra/Mestre.

A atualização pode ocorrer fora da batalha e gerar assets locais.

## 11. Matchup/tipos

Para o inimigo atual calcular e mostrar:

- tipos super efetivos;
- resistências relevantes;
- imunidades/“sem efeito” conforme a tabela usada pelo Pokémon GO;
- indicador resumido do Pokémon atual e das reservas.

A lógica deve ficar fora da View e ser testável.

## 12. Interface e toque

Overlay principal deve ser pass-through e não bloquear o jogo.

- base: `FLAG_NOT_TOUCHABLE` quando não estiver em modo de edição;
- seletores `▼`: usar pequenas janelas touchable somente sobre os controles necessários;
- nunca criar uma janela touchable transparente em tela inteira;
- modo edição pode permitir arrastar/reposicionar blocos;
- persistir posições;
- configuração de escala e opacidade.

## 13. Tela principal/configurações

`MainActivity` / Compose deve conter:

- status/permissão de sobreposição;
- autorização de captura via MediaProjection;
- iniciar/parar assistente;
- liga/CP (1500/2500/Mestre);
- escala do overlay;
- opacidade;
- debug/diagnóstico;
- modo de edição;
- Sobre/créditos.

Crédito em Sobre:

`Desenvolvimento PvPPokGo by: LucianoToscano`
`(84) 99131-9021`

Evitar faixa permanente de crédito durante a batalha.

## 14. Desempenho

- manter apenas o frame mais recente;
- OCR em frequência controlada;
- detector de Fast Move em ROI própria e leve;
- dados/cache fora do loop de renderização;
- invalidar o overlay apenas quando o estado mudar;
- medir bateria, CPU e FPS antes do release.

## 15. Ordem de reconstrução

1. Projeto Android compilável + permissões + foreground service.
2. Overlay passivo e estável.
3. MediaProjection/captura.
4. OCR de nome/CP com fallback manual.
5. Integração do core existente e PvPoke local.
6. Matchup/tipos/reservas manuais.
7. Triângulo Fast + Charged 1/2.
8. Seletores `▼` localizados.
9. Detector validado de Fast Move.
10. Detecção de Charged Move e energia residual.
11. Trocas + timers + memória dos três inimigos.
12. Histórico, modo edição e polimento.
13. Testes de regressão, bateria/CPU/FPS e APK assinado.

## 16. Critérios mínimos de release

- app abre sem crash;
- serviço continua ativo corretamente;
- captura pode ser iniciada/parada sem vazamento;
- overlay aparece e desaparece sem bloquear os toques do jogo;
- perda/recriação de Activity não duplica overlay;
- energia residual passa nos testes;
- troca não zera estado inimigo;
- Fast Move não é incrementado por simples frame/tempo;
- OCR possui fallback manual;
- recursos automáticos incertos indicam confiança e permitem correção;
- nenhuma rotina envia comandos ao Pokémon GO.
