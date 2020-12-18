%
% Plotting latency data from programming assignment 3 from ACS
%

x0 = [10 20 30 40 50 60 70 80 90 100];
y0 = [];
fid = fopen('acertainbookstore-assignment3/latency_local.txt','rt');
while ~feof(fid)
    tline = fgetl(fid);
    y0 = [y0, str2double(tline)];
    disp(tline)
end
fclose(fid);
plot(x0,y0);
title('Latency Plot - Computer 0');
xlabel('Number of Clients');
ylabel('Lantency');
saveas(gcf,'latency_comp0_local.png')
%{
x1 = [];
y1 = [1 2 3 4 5 6 7 8 9 10];
plot(x1,y1);
title('Latency Plot Plot - Computer 1');
xlabel('Number of Clients');
ylabel('Latency Plot');
%}