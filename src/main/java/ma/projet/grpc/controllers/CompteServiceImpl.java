package ma.projet.grpc.controllers;


import io.grpc.stub.StreamObserver;
import ma.projet.grpc.stubs.*;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {
	



    // Simuler une base de données en mémoire
    private final Map<String, Compte> compteDB = new ConcurrentHashMap<>();

    @Override
    public void allComptes(GetAllComptesRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        GetAllComptesResponse.Builder responseBuilder = GetAllComptesResponse.newBuilder();
        responseBuilder.addAllComptes(compteDB.values());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void compteById(GetCompteByIdRequest request, StreamObserver<GetCompteByIdResponse> responseObserver) {
        Compte compte = compteDB.get(request.getId());
        if (compte != null) {
            responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(compte).build());
        } else {
            responseObserver.onError(new Throwable("Compte non trouvé"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void totalSolde(GetTotalSoldeRequest request, StreamObserver<GetTotalSoldeResponse> responseObserver) {
        int count = compteDB.size();
        float sum = 0;
        for (Compte compte : compteDB.values()) {
            sum += compte.getSolde();
        }
        float average = count > 0 ? sum / count : 0;

        SoldeStats stats = SoldeStats.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAverage(average)
                .build();

        responseObserver.onNext(GetTotalSoldeResponse.newBuilder().setStats(stats).build());
        responseObserver.onCompleted();
    }
    
    @Override
    public void findById(FindCompteByIdRequest request, StreamObserver<FindCompteByIdResponse> responseObserver) {
        String id = request.getId();
        Compte compte = compteDB.get(id); // Accès à la "base de données" en mémoire
        
        if (compte != null) {
            // Si le compte est trouvé, on retourne une réponse avec le compte
            responseObserver.onNext(FindCompteByIdResponse.newBuilder().setCompte(compte).build());
        } else {
            // Si le compte n'est pas trouvé, on retourne une erreur
            responseObserver.onError(new Throwable("Compte avec l'ID " + id + " non trouvé"));
        }
        responseObserver.onCompleted();
    }
    
    @Override
    public void deleteById(DeleteCompteByIdRequest request, StreamObserver<DeleteCompteByIdResponse> responseObserver) {
        String id = request.getId();
        if (compteDB.containsKey(id)) {
            // Si le compte existe, on le supprime
            compteDB.remove(id);
            responseObserver.onNext(DeleteCompteByIdResponse.newBuilder().setSuccess(true).build());
        } else {
            // Si le compte n'existe pas, on retourne un succès "false"
            responseObserver.onNext(DeleteCompteByIdResponse.newBuilder().setSuccess(false).build());
        }
        responseObserver.onCompleted();
    }




    @Override
    public void saveCompte(SaveCompteRequest request, StreamObserver<SaveCompteResponse> responseObserver) {
        CompteRequest compteReq = request.getCompte();
        String id = UUID.randomUUID().toString();

        Compte compte = Compte.newBuilder()
                .setId(id)
                .setSolde(compteReq.getSolde())
                .setDateCreation(compteReq.getDateCreation())
                .setType(compteReq.getType())
                .build();

        compteDB.put(id, compte);

        responseObserver.onNext(SaveCompteResponse.newBuilder().setCompte(compte).build());
        responseObserver.onCompleted();
    }
}