# Changelog

## 0.5.15

- As posições do HUD passam a usar como preset oficial as coordenadas exportadas no arquivo PvPPokeGo-0.5.14-config.json enviado pelo usuário.
- Ataque ágil do inimigo aproximado levemente dos dois ataques carregados: distância vertical reduzida de 123 para 112 unidades de referência.
- Áreas de toque dos ataques carregados reposicionadas junto com o visual para manter clique e desenho alinhados.
- Engrenagem customizada removida; launcher passa a usar o ícone padrão de Configurações/Preferências do Android.
- Ícone de configurações mantém uma hitbox confortável, mas com desenho visível pequeno, próximo ao porte do olho/cadeado.
- Mantidos menu reorganizado e controles rápidos de Pausar visão, Remover sobreposição e Parar serviço da 0.5.14.
- Backup exportado passa a usar PvPPokeGo-0.5.15-config.json.
- Corrigida contagem de energia dos carregados inimigos para ataques rápidos de baixo dano: microquedas de 1 px da barra de HP agora são acumuladas em vez de descartadas.
- Rastreamento de energia agora rejeita Fast Move com ganho de energia zero e usa um Fast Move válido como fallback.


## 0.5.14

- Engrenagem redesenhada novamente: menor, sem o círculo grande e com proporção visual próxima ao olho/cadeado.
- Área de toque da engrenagem continua confortável, mas o desenho visível fica discreto.
- Menu da engrenagem reorganizado em seções: Batalha, HUD/Visual, Análise e Sistema.
- Painel do menu ganhou fundo arredondado, espaçamento e hierarquia visual mais limpa.
- Ações destrutivas/operacionais ficam separadas e destacadas no menu.
- Nova ação **Pausar visão/análise**: interrompe OCR/detecção temporariamente sem encerrar o serviço.
- Nova ação **Remover sobreposição**: oculta HUD, ícones, liga e touch targets sem encerrar a captura; pode ser restaurada pela notificação.
- Nova ação **Parar serviço PvPPokeGo** disponível no menu.
- Notificação persistente passa a oferecer três ações: Pausar/Retomar visão, Remover/Mostrar sobreposição e Parar serviço.
- Estado da notificação muda conforme visão pausada ou sobreposição oculta.
- Mantidas as correções e recursos da 0.5.13: layout aprovado, identidade estável do time, confiança de golpes e histórico explicável.


## 0.5.13

- Baseada no 0.5.11 validado, preservando ORIGINAL_ICONS, touch zones, rotação e controles entre batalhas.
- Aplica como preset oficial as posições aprovadas pelo usuário sem sobrescrever posições manuais existentes.
- Migração organiza os dois blocos de reserva por ordem visual (card superior = reserva 1, card inferior = reserva 2) sem mudar os lugares na tela.
- Identidade estável dos três Pokémon do time por slot + nome + PC quando disponível.
- Ordem das reservas passa a girar a partir do Pokémon ativo, acompanhando os cards reais do Pokémon GO após cada troca.
- Golpes carregados são resolvidos como um par distinto; o segundo slot não duplica silenciosamente o primeiro.
- Reservas usam o estado/moveset salvo do próprio Pokémon ao calcular matchup e recomendação.
- Nível de confiança de golpe: ✓ confirmado, M manual, ~ estimado pelo ranking e ? desconhecido.
- Histórico registra Pokémon, origem, confiança e justificativa das decisões do assistente.
- Diagnóstico registra mudanças do moveset associado às reservas para facilitar investigação de erros.
- Engrenagem substituída por desenho vetorial fino, no mesmo estilo visual do olho/cadeado.
- Engrenagem mantém pequena margem do canto para não ficar cortada visualmente.
- Backup exportado passa a usar o nome PvPPokeGo-0.5.13-config.json.


## 0.5.11

### Correções recuperadas da base ORIGINAL_ICONS
- Hit-targets do olho e cadeado voltaram a ser transparentes; os ícones delicados são desenhados no canvas, sem emoji do sistema cobrindo o desenho.
- Cadeado aberto usa zonas pequenas por bloco em vez de uma camada fullscreen, deixando ataques/escudos do jogo clicáveis fora do HUD.
- Cadeado, olho, ⚙ e liga usam touch slop do sistema para distinguir toque de arraste.
- Ao iniciar a captura, `EXTRA_APP_VISIBLE=false` evita que os controles fiquem ocultos/sem resposta.
- Barra de edição ✓/↺ é recriada/elevada corretamente e sai do modo edição de forma segura.
- Olho, cadeado e ⚙ continuam acessíveis entre batalhas.
- Posição do topo deixa de seguir o card detectado a cada frame, evitando salto/flicker.
- Captura recria o VirtualDisplay quando a resolução/orientação muda.
- Reinício sticky sem MediaProjection orienta o usuário a iniciar a captura novamente.
- Removido uso inseguro de `windowManager!!` nesse fluxo.


- Backup/restauração em JSON das configurações, escala, opacidade e posições personalizadas do HUD e menus.
- Importação retorna o layout travado por segurança e não restaura flags transitórias do serviço.
- Histórico local das últimas 50 batalhas com Pokémon, PC, liga, duração e linha do tempo.
- Registro dos ataques rápidos e carregados detectados dos dois lados com tempo relativo ao início.
- Tela para atualizar, expandir detalhes e limpar o histórico.
- Ícones e posições aprovados da base 0.5.10 preservados.
- Convenção oficial: PvPPokeGo-0.5.10.apk, PvPPokeGo-0.5.11.apk, PvPPokeGo-0.5.12.apk e assim por diante.

## 0.3.0-v03-overlay-counter

- Charged Moves reposicionados ~30 px para cima na referência 864x1536, aproximando-os do Fast Move.
- Círculos dos golpes aumentados e geometria desacoplada do antigo multiplicador 0.8.
- Indicador do Pokémon atual movido para fora do card original.
- Topo mostra até quatro tipos fortes, seta e os tipos do oponente.
- Ícones de tipo procedurais em estilo visual, sem usar assets do Pokémon GO.
- `BattleFrameEventDetector`: Fast Move detectado primariamente pela queda da barra de HP, com debounce baseado na duração do golpe.
- Fallback por pico de movimento apenas quando a barra de HP não está disponível.
- OCR de prompt de Charged Move e reset automático, preservando energia residual quando conhecida.
- Memória da equipe do jogador via OCR da tela de equipe e aprendizado por Pokémon ativo.
- Indicadores e tipos das reservas ancorados aos cards laterais.
- Debug mostra fonte/confiança do detector, HP, movimento, energia e equipe aprendida.
