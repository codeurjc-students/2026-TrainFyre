package es.codeurjc.students.trainfyre.users.application.port.in;

import es.codeurjc.students.trainfyre.common.UseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.query.GetUserByIdQuery;
import es.codeurjc.students.trainfyre.users.domain.UserDetails;

public interface GetUserByIdUseCase extends UseCase<GetUserByIdQuery, UserDetails> {
}
