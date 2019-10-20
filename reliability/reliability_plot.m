%% INIT
max_entries = 755;

%% A: Period = 200, Delta = 20

%%% Load data
% WITH NOISE
AC10N200 = load('IATrel_P200_D20_C100000_N200.csv');
AC10N200 = AC10N200(1:max_entries);
AC5N200 = load('IATrel_P 200_D20_C50000_N200.csv');
AC5N200 = AC5N200(1:max_entries);
AC1N200 = load('IATrel_P200_D20_C10000_N200.csv');
AC1N200 = AC1N200(1:max_entries);

% WITHOUT NOISE
AC10N0 = load('IATrel_P200_D20_C100000_N0.csv');
AC10N0 = AC10N0(1:max_entries);
AC5N0 = load('IATrel_P200_D20_C50000_N0.csv');
AC5N0 = AC5N0(1:max_entries);
AC1N0 = load('IATrel_P200_D20_C10000_N0.csv');
AC1N0 = AC1N0(1:max_entries);

%%% Plot data
%% Noise effect on 100k channel
histogram(AC10N200, linspace(170,230), 'DisplayName','Noise'); hold on;
histogram(AC10N0, linspace(170,230), 'DisplayName','No noise'); hold on; legend('show'); hold on;

%% Noise effect on 10k channel
histogram(AC1N200, linspace(170,230), 'DisplayName','Noise'); hold on;
histogram(AC1N0, linspace(170,230), 'DisplayName','No noise'); hold on; legend('show'); hold on;

%% Bus speed effect, no noise
histogram(AC1N0, linspace(170,230), 'DisplayName','Low bus speed'); hold on;
histogram(AC10N0, linspace(170,230), 'DisplayName','High bus speed'); hold on; legend('show'); hold on;

%% Bus speed effect, noise
histogram(AC1N200, linspace(170,230), 'DisplayName','Low bus speed'); hold on;
histogram(AC10N200, linspace(170,230), 'DisplayName','High bus speed'); hold on; legend('show'); hold on;

%% B: Period = 20, Delta = 10

%%% Load data
% WITH NOISE
BC10N20 = load('IATrel_P20_D10_C100000_N20.csv');
BC10N20 = BC10N20(1:max_entries);
BC5N20 = load('IATrel_P20_D10_C50000_N20.csv');
BC5N20 = BC5N20(1:max_entries);
BC1N20 = load('IATrel_P20_D10_C10000_N20.csv');
BC1N20 = BC1N20(1:max_entries);

% WITHOUT NOISE
BC10N0 = load('IATrel_P20_D10_C100000_N0.csv');
BC10N0 = BC10N0(1:max_entries);
BC5N0 = load('IATrel_P20_D10_C50000_N0.csv');
BC5N0 = BC5N0(1:max_entries);
BC1N0 = load('IATrel_P20_D10_C10000_N0.csv');
BC1N0 = BC1N0(1:max_entries);

%%% Plot data
%% Noise effect on 100k channel
histogram(BC10N20, linspace(0,40), 'DisplayName','Noise'); hold on;
histogram(BC10N0, linspace(0,40), 'DisplayName','No noise'); hold on; legend('show'); hold on;

%% Noise effect on 10k channel
histogram(BC1N20, linspace(0,40), 'DisplayName','Noise'); hold on;
histogram(BC1N0, linspace(0,40), 'DisplayName','No noise'); hold on; legend('show'); hold on;

%% Bus speed effect, no noise
histogram(BC1N0, linspace(0,40), 'DisplayName','Low bus speed'); hold on;
histogram(BC10N0, linspace(0,40), 'DisplayName','High bus speed'); hold on; legend('show'); hold on;

%% Bus speed effect, noise
histogram(BC1N20, linspace(0,40), 'DisplayName','Low bus speed'); hold on;
histogram(BC10N20, linspace(0,40), 'DisplayName','High bus speed'); hold on; legend('show'); hold on;

%% C: Period = 100, Delta = 10

%%% Load data
CC5N10 = load('IATrel_P100_D10_C50000_N10.csv');
CC5N10 = CC5N10(1:max_entries);
CC5N0 = load('IATrel_P100_D10_C50000_N0.csv');
CC5N0 = CC5N0(1:max_entries);
CC5N100 = load('IATrel_P100_D10_C50000_N100.csv');
CC5N100 = CC5N100(1:max_entries);

%%% Plot data
%% Noise effect on 50k channel
histogram(CC5N10, linspace(80,120), 'DisplayName','High noise'); hold on;
histogram(CC5N100, linspace(80,120), 'DisplayName','Low noise'); hold on;
histogram(CC5N0, linspace(80,120), 'DisplayName','No noise'); hold on; legend('show'); hold on;