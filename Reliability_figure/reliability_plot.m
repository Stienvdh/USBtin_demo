%% Load data
data = load('reliability.csv');

C10N0 = data(1:6);
C100N0 = data(7:12);
C10N50 = data(13:18);
C100N50 = data(19:24);

%% Plot
xvalues = linspace(7,2,6);
ymatrix = [C10N0 C10N50 C100N0 C100N50];
createfigure(xvalues, ymatrix);