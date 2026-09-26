# PvPPokeGo — Fase 2

Build de teste para validar no aparelho real:

- contador automático de ataques ágeis por mudança visual da barra de HP;
- energia estimada acumulada entre ataques;
- após ataque carregado inimigo, subtrair o custo do golpe e preservar energia residual;
- recalcular automaticamente quantos ataques ágeis faltam para cada carregado;
- ícones visuais de tipo no overlay em vez de abreviações de duas letras;
- OCR de nome/PC continua em baixa frequência para reduzir custo;
- painel DEBUG temporariamente mantido para conferir contagem e energia.

Critério de teste: a contagem deve subir uma vez por ataque ágil adversário, sem duplicar a animação de perda de HP. Quando um ataque carregado for detectado, os contadores devem ser recalculados a partir da energia restante, sem zerar automaticamente salvo quando o custo consumir toda a energia estimada.
