%% Load data
IAT = load('IAT_P200_D10_C100000_N0.csv');
IAT_noise = load('IAT_P200_D10_C10000_N2.csv');
ITT = load('ITT_P200_D10_C100000_N0.csv');
ITT = ITT(1:64,:);
ITT_noise = load('ITT_P200_D10_C10000_N2.csv');
IAT = IAT(1:64,:);
IAT_noise = IAT_noise(1:64,:);
ITT_noise = ITT_noise(1:64,:);

%% Offset
offset = ITT(1,2);
IAT(:,2) = IAT(:,2) - offset;
offset_noise = ITT_noise(1,2);
IAT_noise(:,2) = IAT_noise(:,2) - offset_noise;
ITT_noise(:,2) = ITT_noise(:,2) - offset_noise;
ITT(:,2) = ITT(:,2) - offset;
ITT(:,1) = ITT(:,1) + 3;

%% Plot
createfigure(ITT(:,2), ITT(:,1), IAT(:,2), IAT(:,1), IAT_noise(:,2), IAT_noise(:,1));
