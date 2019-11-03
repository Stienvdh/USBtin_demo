%% Load data
IAT = load('IAT_P200_D20_C10000_N0.csv');
ITT = load('ITT_P200_D20_C10000_N0.csv');
ITT = ITT(1:64,:);
IAT = IAT(1:64,:);

%% Offset
offset = ITT(1,2);
IAT(:,2) = IAT(:,2) - offset;
ITT(:,2) = ITT(:,2) - offset;

%% Plot
createfigure(ITT(:,2), ITT(:,1), IAT(:,2), IAT(:,1));
