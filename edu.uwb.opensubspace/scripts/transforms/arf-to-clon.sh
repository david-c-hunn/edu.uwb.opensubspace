
# head -n 37 Databases/real_world_data/breast.arff \
#     | sed '/^@/g' \
#     | sed '/^\s*$/d' \
#     | awk 'BEGIN { FS = ","; OFS = ","; } { NF--; $1 = $1; }1;'


dbs="Databases/*/*.arff"
outdir="../../carti-clon/dbs"

for db in $dbs
do
    outfile=${db/Databases/${outdir}}

    echo "transformaing ${db}, dest=${outfile}"

    mkdir -p "${outfile%/*}"

    cat ${db} \
        | sed '/^@/g' \
        | sed '/^\s*$/d' \
        | awk 'BEGIN { FS = ","; } { NF--; $1 = $1; }1;' \
        > ${outfile}
done

echo "Done"