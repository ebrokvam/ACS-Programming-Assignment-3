%
% Plotting throughput data from programming assignment 3 from ACS
%

x = [10 20 30 40 50 60 70 80 90 100];

y0 = [];
fid0 = fopen('acertainbookstore-assignment3/throughput_local.txt','rt');
while ~feof(fid0)
    tline = fgetl(fid0);
    y0 = [y0, str2double(tline)];
    disp(tline)
end
fclose(fid0);

y1 = [];
fid1 = fopen('acertainbookstore-assignment3/throughput_rpc.txt','rt');
while ~feof(fid1)
    tline = fgetl(fid1);
    y1 = [y1, str2double(tline)];
    disp(tline)
end
fclose(fid1);

plot(x, y0,'-+b', x, y1,'-*r');
legend('Local','RPC');
title('Throughput - Computer 1');
xlabel('Number of Clients');
ylabel('Agg. Throughput');
saveas(gcf,'throughput_comp1.png')

%{
x1 = [];
y1 = [1 2 3 4 5 6 7 8 9 10];
plot(x1,y1);
title('Throughput Plot - Computer 1');
xlabel('Number of Clients');
ylabel('Agg. Throughput');
%}
