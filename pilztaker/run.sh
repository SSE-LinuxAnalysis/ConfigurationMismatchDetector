#!/bin/bash

base="/home/suh/SPLC17-ConfigMismatches/linux-4.4.1/"
csvFile="/home/suh/linux-4.4.1.csv"

files="`cd $base && find . \( -name *.c -or -name *.h -or -name *.S -or  -name *.C -or -name *.H -or -name *.s \) -not -name skbuff.h -not -name pgtable-64.h -not -name espfix_64.c`"

count=`echo $files | wc -w`
echo "Running for $count files" >&2
done=1

echo "filename;line start;line end;type;indentation;starting if;condition;normalized condition" > $csvFile
for file in $files
do
    echo -n "$done / $count " >&2
    done=$(($done+1))
    ./pilztaker $base $file >> $csvFile
done

